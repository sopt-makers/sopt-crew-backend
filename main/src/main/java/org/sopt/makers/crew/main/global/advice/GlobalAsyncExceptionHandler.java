package org.sopt.makers.crew.main.global.advice;

import java.lang.reflect.Method;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GlobalAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

	@Override
	public void handleUncaughtException(@NonNull Throwable ex, @NonNull Method method, @Nullable Object... params) {
		if (params == null) {
			log.error("비동기 작업 중 예외 발생! Method: [{}], Params: [null], Exception: [{}]",
				method.getName(), ex.getMessage(), ex);
			return;
		}

		String paramValues = java.util.Arrays.toString(params);

		log.error("비동기 작업 중 예외 발생! Method: [{}], Params: [{}], Exception: [{}]",
			method.getName(), paramValues, ex.getMessage(), ex);
	}
}
