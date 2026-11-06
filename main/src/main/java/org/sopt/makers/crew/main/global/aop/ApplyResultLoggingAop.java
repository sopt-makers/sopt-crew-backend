package org.sopt.makers.crew.main.global.aop;

import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.sopt.makers.crew.main.entity.meeting.MeetingReader;
import org.sopt.makers.crew.main.global.exception.BaseException;
import org.sopt.makers.crew.main.global.exception.ErrorStatus;
import org.sopt.makers.crew.main.global.util.Time;
import org.sopt.makers.crew.main.meeting.v2.dto.request.MeetingV2ApplyMeetingDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 모임 신청 결과를 요청당 한 줄로 남긴다. (#956, #910 참고)
 *
 * <p>신청 경로는 {@code ExecutionLoggingAop}에서 제외되므로 이 로그가 신청 추적과 사후 분석의 기준이 된다.
 * 컨트롤러 단에서 남겨서 rate limit(429), Turnstile(403), in-flight guard(423) 거절도 함께 기록된다.</p>
 *
 * <pre>
 * [APPLY_RESULT] type=EVENT result=SUCCESS meetingId=1 userId=2 status=201 reason=- elapsedMs=35
 *                requestedAt=2026-11-06T12:00:00.412 sinceOpenMs=412
 * [APPLY_RESULT] type=EVENT result=FAILURE meetingId=1 userId=2 status=429 reason=TOO_MANY_REQUESTS ... sinceOpenMs=-
 * </pre>
 *
 * <ul>
 *   <li>elapsedMs: 요청 처리 시간 (로그용 모임 조회 시간은 제외)</li>
 *   <li>requestedAt: 요청이 들어온 시각</li>
 *   <li>sinceOpenMs: 모임 신청 시작 시각(startDate)부터 요청이 들어온 시각까지. 음수면 오픈 전 요청.
 *       오픈 직후 수백 ms 안에 반복해서 들어오는 계정은 매크로를 의심할 수 있다.
 *       거절된 요청(429·403·423)과 없는 모임 요청은 조회하지 않고 '-'로 남긴다.</li>
 * </ul>
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ApplyResultLoggingAop {

	static final String TYPE_GENERAL = "GENERAL";
	static final String TYPE_EVENT = "EVENT";
	private static final String NONE = "-";
	private static final Map<String, String> ERROR_STATUS_NAMES = Arrays.stream(ErrorStatus.values())
		.collect(Collectors.toMap(ErrorStatus::getErrorCode, Enum::name, (first, second) -> first));

	private final MeetingReader meetingReader;
	private final Time time;

	@Around("execution(* org.sopt.makers.crew.main.meeting.v2.MeetingV2Controller.applyGeneralMeeting(..))")
	public Object logGeneralApply(ProceedingJoinPoint pjp) throws Throwable {
		return logApplyResult(pjp, TYPE_GENERAL);
	}

	@Around("execution(* org.sopt.makers.crew.main.meeting.v2.MeetingV2Controller.applyEventMeeting(..))")
	public Object logEventApply(ProceedingJoinPoint pjp) throws Throwable {
		return logApplyResult(pjp, TYPE_EVENT);
	}

	Object logApplyResult(ProceedingJoinPoint pjp, String type) throws Throwable {
		LocalDateTime requestedAt = time.now();
		long startedAt = System.nanoTime();
		Integer meetingId = findArg(pjp.getArgs(), MeetingV2ApplyMeetingDto.class, MeetingV2ApplyMeetingDto::getMeetingId);
		Integer userId = findArg(pjp.getArgs(), Principal.class, ApplyResultLoggingAop::toUserId);

		try {
			Object result = pjp.proceed();
			long elapsedMs = elapsedMs(startedAt);
			log.info("[APPLY_RESULT] type={} result=SUCCESS meetingId={} userId={} status={} reason={} "
					+ "elapsedMs={} requestedAt={} sinceOpenMs={}",
				type, meetingId, userId, HttpStatus.CREATED.value(), NONE,
				elapsedMs, requestedAt, sinceOpenMs(meetingId, requestedAt));
			return result;
		} catch (Throwable e) {
			// 신청 처리 시간만 기록하도록 모임 조회 전에 측정한다
			long elapsedMs = elapsedMs(startedAt);
			log.warn("[APPLY_RESULT] type={} result=FAILURE meetingId={} userId={} status={} reason={} "
					+ "elapsedMs={} requestedAt={} sinceOpenMs={}",
				type, meetingId, userId, statusOf(e), reasonOf(e),
				elapsedMs, requestedAt, shouldResolveOpenTime(e) ? sinceOpenMs(meetingId, requestedAt) : NONE);
			throw e;
		}
	}

	/**
	 * 실패한 신청 중 모임 오픈 시각을 조회할 대상인지 판단한다.
	 *
	 * <p>rate limit(429), Turnstile(403), 동시 요청(423) 거절과 존재하지 않는 모임 요청은 조회하지 않는다.
	 * 거절된 요청이 캐시 미스로 DB를 조회해 부하를 키우지 않도록 하기 위해서다.
	 * 이 경우에도 requestedAt이 남으므로 모임 시작 시각과 비교해 사후에 계산할 수 있다.</p>
	 */
	static boolean shouldResolveOpenTime(Throwable e) {
		if (!(e instanceof BaseException baseException)) {
			return false;
		}
		return baseException.getStatusCode() == HttpStatus.BAD_REQUEST.value()
			&& !ErrorStatus.NOT_FOUND_MEETING.name().equals(reasonOf(e));
	}

	/**
	 * 모임 정보는 캐시(MeetingReader)에서 읽어 신청마다 DB를 조회하지 않는다.
	 * 로그를 남기다 실패해도 신청 결과에는 영향을 주지 않도록 실패 시 null을 반환한다.
	 */
	private Long sinceOpenMs(Integer meetingId, LocalDateTime requestedAt) {
		if (meetingId == null) {
			return null;
		}
		try {
			LocalDateTime openAt = meetingReader.getMeetingById(meetingId).getStartDate();
			return openAt == null ? null : Duration.between(openAt, requestedAt).toMillis();
		} catch (RuntimeException e) {
			return null;
		}
	}

	static int statusOf(Throwable e) {
		if (e instanceof BaseException baseException && baseException.getHttpStatus() != null) {
			return baseException.getStatusCode();
		}
		return HttpStatus.INTERNAL_SERVER_ERROR.value();
	}

	/**
	 * ErrorStatus의 errorCode 값에는 한글 메시지가 들어 있어 로그 검색에 불편하므로 enum 이름으로 바꿔 남긴다.
	 */
	static String reasonOf(Throwable e) {
		if (e instanceof BaseException baseException && baseException.getErrorCode() != null) {
			return ERROR_STATUS_NAMES.getOrDefault(baseException.getErrorCode(), e.getClass().getSimpleName());
		}
		return e.getClass().getSimpleName();
	}

	private static long elapsedMs(long startedAtNanos) {
		return Duration.ofNanos(System.nanoTime() - startedAtNanos).toMillis();
	}

	private static Integer toUserId(Principal principal) {
		try {
			return Integer.valueOf(principal.getName());
		} catch (RuntimeException e) {
			return null;
		}
	}

	private static <T> Integer findArg(Object[] args, Class<T> type, Function<T, Integer> extractor) {
		return Arrays.stream(args)
			.filter(type::isInstance)
			.map(type::cast)
			.findFirst()
			.map(extractor)
			.orElse(null);
	}
}
