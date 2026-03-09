package org.sopt.makers.crew.main.global.config;

import java.util.Map;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class MdcTaskDecorator implements TaskDecorator {

	@Override
	public Runnable decorate(Runnable runnable) {
		Map<String, String> contextMap = MDC.getCopyOfContextMap();
		SecurityContext securityContext = SecurityContextHolder.getContext();
		return () -> {
			Map<String, String> previousMdc = MDC.getCopyOfContextMap();
			SecurityContext previousSecurity = SecurityContextHolder.getContext();
			try {
				if (contextMap != null) {
					MDC.setContextMap(contextMap);
				} else {
					MDC.clear();
				}
				SecurityContextHolder.setContext(securityContext);
				runnable.run();
			} finally {
				if (previousMdc != null) {
					MDC.setContextMap(previousMdc);
				} else {
					MDC.clear();
				}
				SecurityContextHolder.setContext(previousSecurity);
			}
		};
	}
}
