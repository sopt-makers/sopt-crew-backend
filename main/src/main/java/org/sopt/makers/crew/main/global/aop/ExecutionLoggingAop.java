package org.sopt.makers.crew.main.global.aop;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import lombok.extern.log4j.Log4j2;

@Aspect
@Component
@Log4j2
@Profile("!test")
public class ExecutionLoggingAop {

	/**
	 * @implNote : 일부 클래스를 제외하고, 모든 클래스의 메서드의 시작과 끝을 로깅한다.
	 * @implNote : 제외 클래스 - global 패키지, config 관련 패키지, Test 클래스, redis 클래스
	 * @implNote : 제외 클래스(Spike Traffic 최적화) - ApplyTransactionService,
	 *           ApplyReadService, SpikeApplyProfiler,
	 *           MeetingV2Controller.applyEventMeeting,
	 *           MeetingV2ServiceImpl.applyEventMeetingWithLock
	 */
	@Around("execution(* org.sopt.makers.crew.main..*(..)) "
			+ "&& !within(org.sopt.makers.crew.main.global..*) "
			+ "&& !within(org.sopt.makers.crew.main.external.s3.config..*)"
			+ "&& !within(org.sopt.makers.crew.main.external.redis..*) "
			+ "&& !within(org.sopt.makers.crew.main.meeting.v2.service.ApplyTransactionService) "
			+ "&& !within(org.sopt.makers.crew.main.meeting.v2.service.ApplyReadService) "
			+ "&& !within(org.sopt.makers.crew.main.meeting.v2.service.SpikeApplyProfiler) "
			+ "&& !execution(* org.sopt.makers.crew.main.meeting.v2.service.MeetingV2ServiceImpl.applyEventMeetingWithLock(..)) "
			+ "&& !execution(* org.sopt.makers.crew.main.meeting.v2.MeetingV2Controller.applyEventMeeting(..)) ")
	public Object logExecutionTrace(ProceedingJoinPoint pjp) throws Throwable {
		String className = pjp.getSignature().getDeclaringType().getSimpleName();
		String methodName = pjp.getSignature().getName();
		String task = className + "." + methodName;

		// 1) 메서드 시작부 로깅 (MDC에서 컨텍스트 정보 활용)
		String requestInfo = MDC.get("requestInfo");
		String userId = MDC.get("userId");
		if (requestInfo != null) {
			log.info("[START] {} | {} | userId={}", task, requestInfo, userId);
		} else {
			log.info("[START] {} (No HTTP RequestContext)", task);
		}

		// 2) 파라미터 로깅
		logMethodParams(pjp.getArgs());

		// 3) 실제 메서드(핵심 로직) 실행 + stopwatch
		StopWatch sw = new StopWatch();
		sw.start();
		Object result;
		try {
			result = pjp.proceed();
		} catch (Exception e) {
			log.warn("[ERROR] {}.{} 메서드 예외 발생 : {}", className, methodName, e.getMessage());
			throw e;
		}
		sw.stop();

		// 4) 메서드 종료부 로깅
		log.info("[END] {} --> {} ms", task, sw.getTotalTimeMillis());

		return result;
	}

	private void logMethodParams(Object[] paramArgs) {
		if (paramArgs.length == 0) {
			return;
		}
		String params = Arrays.stream(paramArgs)
				.map(arg -> arg == null ? "null" : arg.getClass().getSimpleName() + "=" + arg)
				.collect(Collectors.joining(", "));
		log.info("[Params] {}", params);
	}

}
