package org.sopt.makers.crew.main.global.security.turnstile;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.time.Duration;

import org.sopt.makers.crew.main.global.exception.ForbiddenException;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import lombok.extern.slf4j.Slf4j;
import reactor.netty.http.client.HttpClient;

/**
 * Cloudflare Turnstile 토큰 서버 검증
 * https://developers.cloudflare.com/turnstile/get-started/server-side-validation/
 */
@Slf4j
@Component
public class TurnstileVerifier {

	private static final String SITEVERIFY_BASE_URL = "https://challenges.cloudflare.com";
	private static final String SITEVERIFY_PATH = "/turnstile/v0/siteverify";
	private static final int CONNECT_TIMEOUT_MILLIS = 2000;
	private static final Duration RESPONSE_TIMEOUT = Duration.ofSeconds(3);

	private final TurnstileProperties properties;
	private final WebClient webClient;

	public TurnstileVerifier(TurnstileProperties properties) {
		validateConfiguration(properties);
		this.properties = properties;
		this.webClient = WebClient.builder()
			.baseUrl(SITEVERIFY_BASE_URL)
			.clientConnector(new ReactorClientHttpConnector(HttpClient.create()
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MILLIS)
				.responseTimeout(RESPONSE_TIMEOUT)))
			.build();
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
			response = webClient.post()
				.uri(SITEVERIFY_PATH)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body(BodyInserters.fromFormData("secret", properties.getSecret())
					.with("response", token))
				.retrieve()
				.bodyToMono(TurnstileVerifyResponse.class)
				.block(RESPONSE_TIMEOUT.plusSeconds(1));
		} catch (Exception e) {
			// Cloudflare 장애로 전체 신청이 막히지 않도록 기본은 통과(fail-open)
			log.error("[turnstile] siteverify 통신 실패 userId={} failOpen={}", userId, properties.isFailOpen(), e);
			if (!properties.isFailOpen()) {
				fail(userId, "siteverify_unavailable", null);
			}
			return;
		}

		if (response == null || !response.success()) {
			fail(userId, "invalid_token", response);
			return;
		}
		if (properties.getHostnames() == null || !properties.getHostnames().contains(response.hostname())) {
			fail(userId, "hostname_mismatch", response);
			return;
		}
		if (!action.equals(response.action())) {
			fail(userId, "action_mismatch", response);
		}
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
