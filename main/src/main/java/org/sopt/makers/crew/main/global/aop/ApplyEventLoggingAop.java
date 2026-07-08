package org.sopt.makers.crew.main.global.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.sopt.makers.crew.main.meeting.v2.dto.request.MeetingV2ApplyMeetingDto;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

/**
 * 행사 신청 경로 전용 경량 AOP.
 * {@code ExecutionLoggingAop}가 이 경로를 pointcut에서 제외하는 대신,
 * 신청 추적에 꼭 필요한 최소 정보(meetingId, userId, 결과, 소요시간)만 남긴다.
 * 이 클래스의 로거는 log4j2-spring.xml에서 AsyncLogger로 지정되어 있어,
 * 로깅 I/O가 요청 스레드를 막지 않는다.
 */
@Aspect
@Component
@Log4j2
@Profile("!test")
public class ApplyEventLoggingAop {

	@Around("execution(* org.sopt.makers.crew.main.meeting.v2.service.MeetingV2ServiceImpl"
		+ ".applyEventMeetingWithAdmissionControl(..))")
	public Object logApplyEvent(ProceedingJoinPoint pjp) throws Throwable {
		MeetingV2ApplyMeetingDto requestBody = (MeetingV2ApplyMeetingDto)pjp.getArgs()[0];
		Integer userId = (Integer)pjp.getArgs()[1];
		Integer meetingId = requestBody.getMeetingId();

		long startedAt = System.currentTimeMillis();
		try {
			Object result = pjp.proceed();
			log.info("[APPLY_SUCCESS] type=EVENT meetingId={} userId={} elapsedMs={}",
				meetingId, userId, System.currentTimeMillis() - startedAt);
			return result;
		} catch (Exception exception) {
			log.warn("[APPLY_FAILURE] type=EVENT meetingId={} userId={} elapsedMs={} reason={}",
				meetingId, userId, System.currentTimeMillis() - startedAt, exception.getClass().getSimpleName());
			throw exception;
		}
	}
}
