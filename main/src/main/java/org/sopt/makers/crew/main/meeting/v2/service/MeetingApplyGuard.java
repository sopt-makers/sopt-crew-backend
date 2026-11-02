package org.sopt.makers.crew.main.meeting.v2.service;

import org.sopt.makers.crew.main.global.security.turnstile.TurnstileVerifier;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 모임 신청 전 자동화 요청 방어 (rate limit → Turnstile 순)
 * 외부 호출(Turnstile)이 DB 트랜잭션/유저 락을 붙잡지 않도록 트랜잭션 바깥(컨트롤러)에서 호출한다.
 */
@Component
@RequiredArgsConstructor
public class MeetingApplyGuard {

	public static final String TURNSTILE_TOKEN_HEADER = "X-Turnstile-Token";
	public static final String TURNSTILE_ACTION_APPLY = "apply";

	private final ApplyRateLimiter applyRateLimiter;
	private final TurnstileVerifier turnstileVerifier;

	public void validate(Integer userId, String turnstileToken) {
		applyRateLimiter.check(userId);
		turnstileVerifier.verify(turnstileToken, TURNSTILE_ACTION_APPLY, userId);
	}
}
