package org.sopt.makers.crew.main.global.aop;

import java.lang.reflect.Field;
import java.util.Objects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;

@Aspect
@Component
@Log4j2
@Profile("!test & !traffic")
public class ExecutionLoggingAop {

	private static final String UNKNOWN_USER = "unknownUser";

	/**
	 * @implNote : 일부 클래스를 제외하고, 모든 클래스의 메서드의 시작과 끝을 로깅한다.
	 * @implNote : 제외 클래스 - global 패키지, config 관련 패키지, Test 클래스, redis 클래스
	 */
	@Around("execution(* org.sopt.makers.crew.main..*(..)) "
		+ "&& !within(org.sopt.makers.crew.main.global..*) "
		+ "&& !within(org.sopt.makers.crew.main.external.s3.config..*)"
		+ "&& !within(org.sopt.makers.crew.main.external.redis..*) ")
	public Object logExecutionTrace(ProceedingJoinPoint pjp) throws Throwable {
		// 추출 - 요청/메서드 컨텍스트 정보 및 클래스/메서드명
		RequestContextInfo ctxInfo = extractRequestContext();
		String className = pjp.getSignature().getDeclaringType().getSimpleName();
		String methodName = pjp.getSignature().getName();

		// 1) 메서드 시작부 로깅
		logMethodStart(className, methodName, ctxInfo);

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
		logMethodEnd(className, methodName, sw.getTotalTimeMillis());

		return result;
	}

	/**
	 * 현재 스레드에 바인딩된 RequestContext 정보(HTTP request, 메서드, userId 등)를 추출
	 * 비동기(@Async)나 Non-HTTP 호출 시에는 request가 없으므로 null로 처리
	 */
	private RequestContextInfo extractRequestContext() {
		// RequestContextHolder (HTTP request)
		HttpServletRequest request = null;
		RequestMethod httpMethod = null;
		try {
			ServletRequestAttributes sra =
				(ServletRequestAttributes)RequestContextHolder.currentRequestAttributes();
			request = sra.getRequest();
			httpMethod = RequestMethod.valueOf(request.getMethod());
		} catch (IllegalStateException e) {
			log.debug("[ExecutionLoggingAop] No thread-bound request context => skip request-based logging");
		}

		// SecurityContextHolder (인증 정보)
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String userId = UNKNOWN_USER;
		if (authentication != null && authentication.isAuthenticated()) {
			userId = String.valueOf(authentication.getPrincipal());
		}

		return new RequestContextInfo(request, httpMethod, userId);
	}

	/**
	 * 메서드 시작부 로깅 처리
	 */
	private void logMethodStart(String className, String methodName, RequestContextInfo ctxInfo) {
		log.info("");
		log.info("🚨 {} Start", className);

		String task = className + "." + methodName;

		// request가 존재하면 HTTP 관련 로깅
		if (ctxInfo.request() != null && ctxInfo.httpMethod() != null) {
			log.info("[Call Method] {}: {} | Request userId={}",
				ctxInfo.httpMethod(), task, ctxInfo.userId());
		} else {
			log.info("[Call Method] {} (No HTTP RequestContext) | userId={}",
				task, ctxInfo.userId());
		}
	}

	/**
	 * 메서드 파라미터 로깅 처리
	 */
	private void logMethodParams(Object[] paramArgs) {
		for (Object object : paramArgs) {
			if (Objects.nonNull(object)) {
				log.info("[Parameter] {} {}", object.getClass().getSimpleName(), object);

				String packageName = object.getClass().getPackage().getName();
				if (packageName.contains("java")) {
					break;
				}

				Field[] fields = object.getClass().getDeclaredFields();
				for (Field field : fields) {
					field.setAccessible(true); // private 필드에도 접근 가능하도록 설정
					try {
						Object value = field.get(object); // 필드값 가져오기
						log.info("[Field] {} = {}", field.getName(), value);
					} catch (IllegalAccessException e) {
						log.warn("[Field Access Error] Cannot access field: {}", field.getName());
					}
				}
			}
		}
	}

	/**
	 * 메서드 종료부 로깅 처리
	 */
	private void logMethodEnd(String className, String methodName, long executionTime) {
		String task = className + "." + methodName;
		log.info("[ExecutionTime] {} --> {} (ms)", task, executionTime);
		log.info("🚨 {} End", className);
		log.info("");
	}

	/**
	 * Request 관련 정보를 담는 레코드
	 */
	public record RequestContextInfo(
		HttpServletRequest request,
		RequestMethod httpMethod,
		String userId
	) {
	}
}
