package org.sopt.makers.crew.main.global.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code ExecutionLoggingAop}의 메서드 시작·파라미터·종료 로깅에서 제외한다.
 *
 * <p>모임 신청처럼 짧은 시간에 요청이 몰리는 경로는 파라미터 리플렉션 로깅이 병목이 되고,
 * Turnstile 토큰 같은 값이 로그에 남을 수 있어 제외한다. 대신 {@code ApplyResultLoggingAop}가 결과를 한 줄로 남긴다.</p>
 *
 * <p>클래스에 붙이면 그 클래스의 모든 메서드, 메서드에 붙이면 해당 메서드만 제외한다.</p>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NoExecutionLogging {
}
