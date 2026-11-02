package org.sopt.makers.crew.main.global.security.turnstile;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sopt.makers.crew.main.global.exception.ErrorStatus;
import org.sopt.makers.crew.main.global.exception.ForbiddenException;

import com.sun.net.httpserver.HttpServer;

class TurnstileVerifierTest {

	private static final String SECRET = "test-secret";
	private static final String TOKEN = "turnstile-token";
	private static final String HOSTNAME = "playground.sopt.org";
	private static final String ACTION = "apply";
	private static final int USER_ID = 1;
	/** 전체 대기 상한(2초)에 더해 허용할 여유 시간 */
	private static final Duration TIMEOUT_MARGIN = Duration.ofMillis(800);

	private HttpServer server;
	private ExecutorService serverExecutor;
	private String baseUrl;

	private final AtomicInteger responseStatus = new AtomicInteger(200);
	private final AtomicReference<String> responseBody = new AtomicReference<>("");
	private final AtomicReference<Duration> responseDelay = new AtomicReference<>(Duration.ZERO);
	private final AtomicReference<String> lastRequestBody = new AtomicReference<>();
	private final AtomicInteger callCount = new AtomicInteger();

	@BeforeEach
	void setUp() throws IOException {
		// 동시 신청 테스트에서 연결이 거부되지 않도록 backlog를 넉넉히 둔다
		server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 512);
		server.createContext("/turnstile/v0/siteverify", exchange -> {
			callCount.incrementAndGet();
			lastRequestBody.set(URLDecoder.decode(
				new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8), StandardCharsets.UTF_8));
			try {
				Thread.sleep(responseDelay.get().toMillis());
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
			byte[] body = responseBody.get().getBytes(StandardCharsets.UTF_8);
			exchange.getResponseHeaders().add("Content-Type", "application/json");
			exchange.sendResponseHeaders(responseStatus.get(), body.length == 0 ? -1 : body.length);
			try (OutputStream os = exchange.getResponseBody()) {
				os.write(body);
			}
		});
		// 지연 응답 중에도 다른 요청을 처리할 수 있도록 별도 스레드 풀 사용
		serverExecutor = Executors.newCachedThreadPool();
		server.setExecutor(serverExecutor);
		server.start();
		baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
	}

	@AfterEach
	void tearDown() {
		server.stop(0);
		serverExecutor.shutdownNow();
	}

	private TurnstileVerifier verifier(boolean enforce, boolean failOpen) {
		return new TurnstileVerifier(new TurnstileProperties(SECRET, List.of(HOSTNAME), enforce, failOpen), baseUrl);
	}

	private void respond(boolean success, String hostname, String action) {
		responseBody.set(String.format(
			"{\"success\":%s,\"hostname\":\"%s\",\"action\":\"%s\",\"challenge_ts\":\"2026-11-02T00:00:00.000Z\",\"error-codes\":[]}",
			success, hostname, action));
	}

	@Nested
	@DisplayName("검증 결과")
	class VerifyResult {

		@Test
		@DisplayName("성공 응답이고 hostname, action이 일치하면 통과한다")
		void pass() {
			respond(true, HOSTNAME, ACTION);

			assertThatCode(() -> verifier(true, true).verify(TOKEN, ACTION, USER_ID)).doesNotThrowAnyException();
			assertThat(lastRequestBody.get()).contains("secret=" + SECRET).contains("response=" + TOKEN);
		}

		@Test
		@DisplayName("success=false면 CAPTCHA_FAILED 예외가 발생한다")
		void invalidToken() {
			respond(false, HOSTNAME, ACTION);

			assertThatThrownBy(() -> verifier(true, true).verify(TOKEN, ACTION, USER_ID))
				.isInstanceOf(ForbiddenException.class)
				.hasMessage(ErrorStatus.CAPTCHA_FAILED.getErrorCode());
		}

		@Test
		@DisplayName("허용되지 않은 hostname에서 발급된 토큰이면 예외가 발생한다")
		void hostnameMismatch() {
			respond(true, "evil.example.com", ACTION);

			assertThatThrownBy(() -> verifier(true, true).verify(TOKEN, ACTION, USER_ID))
				.isInstanceOf(ForbiddenException.class);
		}

		@Test
		@DisplayName("다른 action으로 발급된 토큰이면 예외가 발생한다")
		void actionMismatch() {
			respond(true, HOSTNAME, "login");

			assertThatThrownBy(() -> verifier(true, true).verify(TOKEN, ACTION, USER_ID))
				.isInstanceOf(ForbiddenException.class);
		}

		@Test
		@DisplayName("토큰이 없으면 Cloudflare를 호출하지 않고 예외가 발생한다")
		void missingToken() {
			assertThatThrownBy(() -> verifier(true, true).verify(null, ACTION, USER_ID))
				.isInstanceOf(ForbiddenException.class);
			assertThatThrownBy(() -> verifier(true, true).verify(" ", ACTION, USER_ID))
				.isInstanceOf(ForbiddenException.class);
			assertThat(callCount.get()).isZero();
		}

		@Test
		@DisplayName("enforce=false면 검증에 실패해도 통과한다")
		void logOnly() {
			respond(false, HOSTNAME, ACTION);

			assertThatCode(() -> verifier(false, true).verify(TOKEN, ACTION, USER_ID)).doesNotThrowAnyException();
			assertThatCode(() -> verifier(false, true).verify(null, ACTION, USER_ID)).doesNotThrowAnyException();
		}
	}

	@Nested
	@DisplayName("통신 장애 (fail-open 대상)")
	class Unavailable {

		@Test
		@DisplayName("응답이 지연되면 대기 상한 안에 끝나고 fail-open이면 통과한다")
		void delayedResponseFailOpen() {
			respond(true, HOSTNAME, ACTION);
			responseDelay.set(Duration.ofSeconds(5));

			long startedAt = System.nanoTime();
			assertThatCode(() -> verifier(true, true).verify(TOKEN, ACTION, USER_ID)).doesNotThrowAnyException();

			assertThat(Duration.ofNanos(System.nanoTime() - startedAt))
				.isLessThan(TurnstileVerifier.TOTAL_TIMEOUT.plus(TIMEOUT_MARGIN));
		}

		@Test
		@DisplayName("응답이 지연되면 대기 상한 안에 끝나고 fail-open이 아니면 예외가 발생한다")
		void delayedResponseFailClosed() {
			respond(true, HOSTNAME, ACTION);
			responseDelay.set(Duration.ofSeconds(5));

			long startedAt = System.nanoTime();
			assertThatThrownBy(() -> verifier(true, false).verify(TOKEN, ACTION, USER_ID))
				.isInstanceOf(ForbiddenException.class);

			assertThat(Duration.ofNanos(System.nanoTime() - startedAt))
				.isLessThan(TurnstileVerifier.TOTAL_TIMEOUT.plus(TIMEOUT_MARGIN));
		}

		@Test
		@DisplayName("5xx 응답이면 fail-open일 때 통과한다")
		void serverError() {
			responseStatus.set(503);
			responseBody.set("{\"message\":\"unavailable\"}");

			assertThatCode(() -> verifier(true, true).verify(TOKEN, ACTION, USER_ID)).doesNotThrowAnyException();
			assertThatThrownBy(() -> verifier(true, false).verify(TOKEN, ACTION, USER_ID))
				.isInstanceOf(ForbiddenException.class);
		}

		@Test
		@DisplayName("429 응답은 일시적인 요청 과다로 보고 fail-open일 때 통과한다")
		void tooManyRequests() {
			responseStatus.set(429);
			responseBody.set("{\"message\":\"too many requests\"}");

			assertThatCode(() -> verifier(true, true).verify(TOKEN, ACTION, USER_ID)).doesNotThrowAnyException();
			assertThatThrownBy(() -> verifier(true, false).verify(TOKEN, ACTION, USER_ID))
				.isInstanceOf(ForbiddenException.class);
		}

		@Test
		@DisplayName("서버에 연결할 수 없으면 fail-open일 때 통과한다")
		void connectionRefused() throws IOException {
			int closedPort;
			try (ServerSocket socket = new ServerSocket(0)) {
				closedPort = socket.getLocalPort();
			}
			TurnstileVerifier verifier = new TurnstileVerifier(
				new TurnstileProperties(SECRET, List.of(HOSTNAME), true, true), "http://127.0.0.1:" + closedPort);

			assertThatCode(() -> verifier.verify(TOKEN, ACTION, USER_ID)).doesNotThrowAnyException();
		}
	}

	@Nested
	@DisplayName("응답 이상 (fail-open 대상 아님)")
	class InvalidResponse {

		@Test
		@DisplayName("4xx 응답이면 fail-open이어도 예외가 발생한다")
		void clientError() {
			responseStatus.set(400);
			responseBody.set("{\"message\":\"bad request\"}");

			assertThatThrownBy(() -> verifier(true, true).verify(TOKEN, ACTION, USER_ID))
				.isInstanceOf(ForbiddenException.class);
		}

		@Test
		@DisplayName("응답을 파싱할 수 없으면 fail-open이어도 예외가 발생한다")
		void malformedResponse() {
			responseBody.set("not-json");

			assertThatThrownBy(() -> verifier(true, true).verify(TOKEN, ACTION, USER_ID))
				.isInstanceOf(ForbiddenException.class);
		}

		@Test
		@DisplayName("응답 본문이 비어 있으면 fail-open이어도 예외가 발생한다")
		void emptyResponse() {
			responseBody.set("");

			assertThatThrownBy(() -> verifier(true, true).verify(TOKEN, ACTION, USER_ID))
				.isInstanceOf(ForbiddenException.class);
		}
	}

	@Nested
	@DisplayName("동시 신청")
	class ConcurrentRequests {

		@Test
		@DisplayName("모집 오픈 순간 200건이 동시에 들어와도 연결 풀 부족 없이 모두 실제로 검증한다")
		void verifyAllConcurrentRequests() throws Exception {
			int concurrentRequests = TurnstileVerifier.MAX_CONNECTIONS;
			respond(true, HOSTNAME, ACTION);
			// 응답에 시간이 걸려야 요청들이 실제로 동시에 연결을 점유한다
			responseDelay.set(Duration.ofMillis(300));

			// fail-open을 끄면 연결 획득 실패 등 통신 장애가 예외로 드러난다
			TurnstileVerifier verifier = verifier(true, false);
			ExecutorService requestExecutor = Executors.newFixedThreadPool(concurrentRequests);
			CountDownLatch startLatch = new CountDownLatch(1);
			List<Future<?>> futures = new ArrayList<>();

			try {
				for (int i = 0; i < concurrentRequests; i++) {
					int userId = i;
					futures.add(requestExecutor.submit(() -> {
						startLatch.await();
						verifier.verify(TOKEN, ACTION, userId);
						return null;
					}));
				}
				startLatch.countDown();

				for (Future<?> future : futures) {
					assertThatCode(() -> future.get(10, TimeUnit.SECONDS)).doesNotThrowAnyException();
				}
				assertThat(callCount.get()).isEqualTo(concurrentRequests);
			} finally {
				requestExecutor.shutdownNow();
				verifier.close();
			}
		}
	}

	@Nested
	@DisplayName("기동 시 설정 검증")
	class ConfigurationValidation {

		@Test
		@DisplayName("enforce=true인데 secret이나 hostnames가 비어 있으면 생성에 실패한다")
		void enforceWithMissingConfig() {
			assertThatThrownBy(() -> new TurnstileVerifier(new TurnstileProperties("", List.of(HOSTNAME), true, true)))
				.isInstanceOf(IllegalStateException.class);
			assertThatThrownBy(() -> new TurnstileVerifier(new TurnstileProperties(SECRET, List.of(), true, true)))
				.isInstanceOf(IllegalStateException.class);
		}

		@Test
		@DisplayName("enforce=false면 설정이 비어 있어도 생성된다")
		void logOnlyWithMissingConfig() {
			assertThatCode(() -> new TurnstileVerifier(new TurnstileProperties("", List.of(), false, true)))
				.doesNotThrowAnyException();
		}
	}
}
