package org.sopt.makers.crew.main.global.metrics;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class EventApplyRequestMatcher {
	private static final String EVENT_APPLY_BASE_PATH = "/meeting/v2";
	private static final String METHOD_POST = "POST";

	private final String eventApplyPath;

	public EventApplyRequestMatcher(@Value("${custom.paths.eventApply}") String eventApplyPath) {
		this.eventApplyPath = eventApplyPath;
	}

	public boolean matches(HttpServletRequest request) {
		return METHOD_POST.equalsIgnoreCase(request.getMethod())
			&& (EVENT_APPLY_BASE_PATH + eventApplyPath).equals(request.getRequestURI());
	}
}
