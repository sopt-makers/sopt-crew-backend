package org.sopt.makers.crew.main.global.metrics;

import static org.sopt.makers.crew.main.global.metrics.SpikeApplyMetrics.REQUEST_MATCHED_ATTRIBUTE;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SpikeApplyRequestSupport {
	private final EventApplyRequestMatcher eventApplyRequestMatcher;
	private final SpikeDiagnosticProperties spikeDiagnosticProperties;

	public boolean isSpikeApplyRequest(HttpServletRequest request) {
		if (Boolean.TRUE.equals(request.getAttribute(REQUEST_MATCHED_ATTRIBUTE))) {
			return true;
		}

		if (!eventApplyRequestMatcher.matches(request)) {
			return false;
		}

		request.setAttribute(REQUEST_MATCHED_ATTRIBUTE, true);
		return true;
	}

	public boolean shouldPersistDiagnosticAttributes(HttpServletRequest request) {
		return spikeDiagnosticProperties.isEnabled() || !isSpikeApplyRequest(request);
	}

	public boolean shouldCaptureClientIp(HttpServletRequest request) {
		return shouldPersistDiagnosticAttributes(request);
	}

	public boolean diagnosticsEnabled() {
		return spikeDiagnosticProperties.isEnabled();
	}
}
