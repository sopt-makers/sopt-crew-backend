package org.sopt.makers.crew.main.global.interceptor;

import org.slf4j.MDC;
import org.sopt.makers.crew.main.global.annotation.SkipAopLogging;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class EventApplyAopSkipInterceptor implements HandlerInterceptor {
	static final String SKIP_AOP_LOGGING = "skipAopLogging";
	static final String SKIP_AOP_LOGGING_APPLIED = EventApplyAopSkipInterceptor.class.getName() + ".applied";
	static final String SKIP_AOP_LOGGING_PREVIOUS = EventApplyAopSkipInterceptor.class.getName() + ".previous";

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		if (shouldSkipAopLogging(handler)) {
			request.setAttribute(SKIP_AOP_LOGGING_APPLIED, true);
			request.setAttribute(SKIP_AOP_LOGGING_PREVIOUS, MDC.get(SKIP_AOP_LOGGING));
			MDC.put(SKIP_AOP_LOGGING, "true");
		}
		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
		Exception ex) {
		if (Boolean.TRUE.equals(request.getAttribute(SKIP_AOP_LOGGING_APPLIED))) {
			String previousValue = (String)request.getAttribute(SKIP_AOP_LOGGING_PREVIOUS);
			if (previousValue == null) {
				MDC.remove(SKIP_AOP_LOGGING);
				return;
			}
			MDC.put(SKIP_AOP_LOGGING, previousValue);
		}
	}

	private boolean shouldSkipAopLogging(Object handler) {
		if (!(handler instanceof HandlerMethod handlerMethod)) {
			return false;
		}
		return handlerMethod.hasMethodAnnotation(SkipAopLogging.class);
	}
}
