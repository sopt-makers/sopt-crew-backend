package org.sopt.makers.crew.main.global.security.turnstile;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Cloudflare Turnstile 설정
 *
 * <ul>
 *   <li>secret: Turnstile secret key (환경변수 TURNSTILE_SECRET_KEY)</li>
 *   <li>hostnames: 위젯이 렌더링되는 프론트 도메인 목록 (siteverify 응답의 hostname 검증용)</li>
 *   <li>enforce: false면 검증 실패 시 로그만 남기고 통과 (점진 적용용)</li>
 *   <li>failOpen: Cloudflare 통신 장애 시 통과시킬지 여부</li>
 * </ul>
 */
@ConfigurationProperties(prefix = "turnstile")
@Getter
@RequiredArgsConstructor
public class TurnstileProperties {
	private final String secret;
	private final List<String> hostnames;
	private final boolean enforce;
	private final boolean failOpen;
}
