package org.sopt.makers.crew.main.meeting.v2.service;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.sopt.makers.crew.main.global.exception.TooManyRequestsException;
import org.sopt.makers.crew.main.global.annotation.NoExecutionLogging;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import lombok.extern.slf4j.Slf4j;

/**
 * 모임 신청 API 사용자별 rate limit (고정 윈도우)
 * 운영 서버가 1대라 인메모리(Caffeine)로 처리. 서버가 늘어나면 Redis 등 공유 저장소로 옮겨야 함.
 */
@Slf4j
@Component
@NoExecutionLogging
public class ApplyRateLimiter {

	private final int maxRequests;
	private final Cache<Integer, AtomicInteger> counters;

	public ApplyRateLimiter(
		@Value("${apply.rate-limit.max-requests:3}") int maxRequests,
		@Value("${apply.rate-limit.window-seconds:10}") long windowSeconds) {
		this.maxRequests = maxRequests;
		// 첫 요청 시점에 엔트리가 생성되고 windowSeconds 후 만료 → 고정 윈도우
		this.counters = Caffeine.newBuilder()
			.expireAfterWrite(Duration.ofSeconds(windowSeconds))
			.maximumSize(100_000)
			.build();
	}

	public void check(Integer userId) {
		int count = counters.get(userId, id -> new AtomicInteger()).incrementAndGet();
		if (count > maxRequests) {
			log.warn("[apply-suspicious] rate_limited userId={} count={}", userId, count);
			throw new TooManyRequestsException(TOO_MANY_REQUESTS.getErrorCode());
		}
	}
}
