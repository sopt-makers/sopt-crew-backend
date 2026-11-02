package org.sopt.makers.crew.main.global.security.turnstile;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

import org.sopt.makers.crew.main.global.exception.ForbiddenException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import io.netty.channel.ChannelOption;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Exceptions;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

/**
 * Cloudflare Turnstile 토큰 서버 검증
 * https://developers.cloudflare.com/turnstile/get-started/server-side-validation/
 *
 * <p>siteverify 호출 결과 처리</p>
 * <ul>
 *   <li>통신 장애(연결 실패, 타임아웃, 429, 5xx): fail-open 설정에 따라 통과 또는 검증 실패</li>
 *   <li>그 외 오류(4xx, 빈 응답, 응답 파싱 실패): 검증 실패로 처리 (enforce=true면 CAPTCHA_FAILED)</li>
 * </ul>
 */
@Slf4j
@Component
public class TurnstileVerifier {

	private static final String SITEVERIFY_BASE_URL = "https://challenges.cloudflare.com";
	private static final String SITEVERIFY_PATH = "/turnstile/v0/siteverify";

	/**
	 * 요청 스레드가 siteverify를 기다리는 시간의 상한.
	 * 운영 서버 1대에서 Cloudflare 지연이 Tomcat 스레드를 붙잡아 다른 API로 번지지 않도록 짧게 둔다.
	 */
	static final Duration TOTAL_TIMEOUT = Duration.ofSeconds(2);
	private static final int CONNECT_TIMEOUT_MILLIS = 1000;
	private static final Duration RESPONSE_TIMEOUT = Duration.ofMillis(1500);

	/**
	 * siteverify 전용 연결 풀 크기.
	 * reactor-netty 기본 공용 풀(연결 16개 + 대기 32개 수준)로는 모집 오픈 순간 최대 200명의 동시 신청을 감당하지 못해
	 * 초과 요청이 즉시 연결 획득 실패 → 통신 장애(fail-open)로 처리되어 검증 없이 통과한다.
	 * 신청 요청은 Tomcat 스레드당 1건씩 동기 호출하므로 Tomcat 기본 최대 스레드 수(200)에 맞춘다.
	 */
	static final int MAX_CONNECTIONS = 200;
	/** Cloudflare가 먼저 끊은 유휴 연결을 재사용하다 실패하지 않도록 일정 시간 쉰 연결은 버린다 */
	private static final Duration MAX_IDLE_TIME = Duration.ofSeconds(20);

	private final TurnstileProperties properties;
	private final ConnectionProvider connectionProvider;
	private final WebClient webClient;

	@Autowired
	public TurnstileVerifier(TurnstileProperties properties) {
		this(properties, SITEVERIFY_BASE_URL);
	}

	/**
	 * 테스트에서 siteverify 서버 주소를 바꾸기 위한 생성자
	 */
	TurnstileVerifier(TurnstileProperties properties, String siteverifyBaseUrl) {
		validateConfiguration(properties);
		this.properties = properties;
		this.connectionProvider = ConnectionProvider.builder("turnstile-siteverify")
			.maxConnections(MAX_CONNECTIONS)
			.pendingAcquireMaxCount(MAX_CONNECTIONS)
			.pendingAcquireTimeout(TOTAL_TIMEOUT)
			.maxIdleTime(MAX_IDLE_TIME)
			.build();
		this.webClient = WebClient.builder()
			.baseUrl(siteverifyBaseUrl)
			.clientConnector(new ReactorClientHttpConnector(HttpClient.create(connectionProvider)
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MILLIS)
				.responseTimeout(RESPONSE_TIMEOUT)))
			.build();
	}

	@PreDestroy
	void close() {
		connectionProvider.dispose();
	}

	/**
	 * 잘못된 설정으로 모든 신청이 403으로 거부되는 것을 막기 위해 기동 시점에 검증한다.
	 * secret이 비어 있으면 siteverify가 missing-input-secret으로 success=false를 응답하고,
	 * hostnames가 비어 있으면 모든 토큰이 hostname 검증에 실패한다.
	 * enforce=false면 신청이 차단되지 않으므로 경고 로그만 남긴다.
	 */
	private static void validateConfiguration(TurnstileProperties properties) {
		boolean secretMissing = properties.getSecret() == null || properties.getSecret().isBlank();
		boolean hostnamesMissing = properties.getHostnames() == null || properties.getHostnames().isEmpty();

		if (!secretMissing && !hostnamesMissing) {
			return;
		}

		String message = String.format(
			"Turnstile 설정이 비어 있습니다. secretMissing=%s, hostnamesMissing=%s", secretMissing, hostnamesMissing);
		if (properties.isEnforce()) {
			throw new IllegalStateException(message + " (turnstile.enforce=true에서는 모든 신청이 차단되므로 기동을 중단합니다)");
		}
		log.warn("[turnstile] {} (enforce=false라 검증 실패 시에도 신청은 통과합니다)", message);
	}

	/**
	 * @param token  프론트 위젯에서 받은 토큰 (cf-turnstile-response)
	 * @param action 위젯의 data-action 값. 다른 용도로 발급된 토큰 재사용 방지
	 * @param userId 로그용
	 */
	public void verify(String token, String action, Integer userId) {
		if (token == null || token.isBlank()) {
			fail(userId, "missing_token", null);
			return;
		}

		TurnstileVerifyResponse response;
		try {
			response = requestSiteverify(token);
		} catch (Exception e) {
			handleSiteverifyError(userId, Exceptions.unwrap(e));
			return;
		}

		if (response == null) {
			log.error("[turnstile] siteverify 응답 본문이 비어 있습니다 userId={}", userId);
			fail(userId, "siteverify_empty_response", null);
			return;
		}
		if (!response.success()) {
			fail(userId, "invalid_token", response);
			return;
		}
		// enforce=false에서는 hostnames가 비어 있어도 기동되므로 null을 방어한다
		if (properties.getHostnames() == null || !properties.getHostnames().contains(response.hostname())) {
			fail(userId, "hostname_mismatch", response);
			return;
		}
		if (!action.equals(response.action())) {
			fail(userId, "action_mismatch", response);
		}
	}

	private TurnstileVerifyResponse requestSiteverify(String token) {
		return webClient.post()
			.uri(SITEVERIFY_PATH)
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.body(BodyInserters.fromFormData("secret", properties.getSecret())
				.with("response", token))
			.retrieve()
			.bodyToMono(TurnstileVerifyResponse.class)
			// 연결·응답 타임아웃과 별개로 전체 대기 시간 상한을 둔다
			.timeout(TOTAL_TIMEOUT)
			.block();
	}

	private void handleSiteverifyError(Integer userId, Throwable error) {
		if (isUnavailable(error)) {
			// Cloudflare 장애로 전체 신청이 막히지 않도록 fail-open 설정 시 통과
			log.warn("[turnstile] siteverify 통신 장애 userId={} failOpen={} error={}",
				userId, properties.isFailOpen(), error.toString());
			if (!properties.isFailOpen()) {
				fail(userId, "siteverify_unavailable", null);
			}
			return;
		}

		// 4xx, 응답 파싱 실패 등은 장애가 아니라 요청·응답 이상이므로 통과시키지 않는다
		log.error("[turnstile] siteverify 응답 처리 실패 userId={}", userId, error);
		fail(userId, "siteverify_error", null);
	}

	/**
	 * fail-open 대상: 연결 실패·연결/응답 타임아웃(WebClientRequestException), 전체 대기 상한 초과,
	 * Cloudflare 429(요청 과다, 일시적)·5xx
	 */
	private static boolean isUnavailable(Throwable error) {
		if (error instanceof TimeoutException || error instanceof WebClientRequestException) {
			return true;
		}
		if (error instanceof WebClientResponseException responseException) {
			return responseException.getStatusCode().is5xxServerError()
				|| responseException.getStatusCode().value() == HttpStatus.TOO_MANY_REQUESTS.value();
		}
		return false;
	}

	private void fail(Integer userId, String reason, TurnstileVerifyResponse response) {
		log.warn("[apply-suspicious] turnstile_failed reason={} userId={} enforce={} hostname={} action={} errorCodes={}",
			reason, userId, properties.isEnforce(),
			response == null ? null : response.hostname(),
			response == null ? null : response.action(),
			response == null ? null : response.errorCodes());

		if (properties.isEnforce()) {
			throw new ForbiddenException(CAPTCHA_FAILED.getErrorCode());
		}
	}
}
