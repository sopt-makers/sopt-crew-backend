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
@Profile("!test")
public class ExecutionLoggingAop {

	/**
	 * @implNote : 일부 클래스를 제외하고, 모든 클래스의 메서드의 시작과 끝을 로깅한다.
	 * @implNote : 제외 클래스 - global 패키지, config 관련 패키지, Test 클래스
	 * */
	@Around("execution(* org.sopt.makers.crew.main..*(..)) "
		+ "&& !within(org.sopt.makers.crew.main.global..*) "
		+ "&& !within(org.sopt.makers.crew.main.external.s3.config..*)"
	)
	public Object logExecutionTrace(ProceedingJoinPoint pjp) throws Throwable {
		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
		RequestMethod httpMethod = RequestMethod.valueOf(request.getMethod());

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Object userId = authentication.getPrincipal().toString();

		String className = pjp.getSignature().getDeclaringType().getSimpleName();
		String methodName = pjp.getSignature().getName();
		String task = className + "." + methodName;

		log.info("");
		log.info("🚨 {} Start", className);
		log.info("[Call Method] " + httpMethod + ": " + task + " | Request userId=" + userId);

		Object[] paramArgs = pjp.getArgs();
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
						log.warn("[Field Access Error] Cannot access field: " + field.getName());
					}
				}
			}
		}

		// 해당 클래스 처리 전의 시간
		StopWatch sw = new StopWatch();
		sw.start();

		Object result = null;

		// 해당 클래스의 메소드 실행
		try {
			result = pjp.proceed();
		} catch (Exception e) {
			log.warn("[ERROR] " + task + " 메서드 예외 발생 : " + e.getMessage());
			throw e;
		}

		// 해당 클래스 처리 후의 시간
		sw.stop();
		long executionTime = sw.getTotalTimeMillis();

		log.info("[ExecutionTime] " + task + " --> " + executionTime + " (ms)");
		log.info("🚨 {} End", className);
		log.info("");

		return result;
	}

}
