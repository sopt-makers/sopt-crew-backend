package org.sopt.makers.crew.main.global.filter;

import static org.sopt.makers.crew.main.global.metrics.SpikeApplyMetrics.CLIENT_IP_ATTRIBUTE;
import static org.sopt.makers.crew.main.global.metrics.SpikeApplyMetrics.METRIC_APP_EDGE_PRE_REQUEST_TOTAL;
import static org.sopt.makers.crew.main.global.metrics.SpikeApplyMetrics.METRIC_APP_EDGE_TOTAL;
import static org.sopt.makers.crew.main.global.metrics.SpikeApplyMetrics.OUTCOME_ERROR;
import static org.sopt.makers.crew.main.global.metrics.SpikeApplyMetrics.OUTCOME_SUCCESS;
import static org.sopt.makers.crew.main.global.metrics.SpikeApplyMetrics.REQUEST_INFO_ATTRIBUTE;
import static org.sopt.makers.crew.main.global.metrics.SpikeApplyMetrics.REQUEST_MATCHED_ATTRIBUTE;
import static org.sopt.makers.crew.main.global.metrics.SpikeApplyMetrics.REQUEST_TOTAL_NANOS_ATTRIBUTE;
import static org.sopt.makers.crew.main.global.metrics.SpikeApplyMetrics.TRACE_ID_ATTRIBUTE;
import static org.sopt.makers.crew.main.global.metrics.SpikeApplyMetrics.USER_ID_ATTRIBUTE;

import java.io.IOException;

import org.slf4j.MDC;
import org.sopt.makers.crew.main.global.metrics.EventApplyRequestMatcher;
import org.sopt.makers.crew.main.global.metrics.SpikeDiagnosticProperties;
import org.sopt.makers.crew.main.global.metrics.SpikeApplyMetricRecorder;
import org.sopt.makers.crew.main.global.metrics.SpikeApplyRuntimeConfig;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class SpikeApplyAppEdgeMetricsFilter extends OncePerRequestFilter {
	private final EventApplyRequestMatcher eventApplyRequestMatcher;
	private final SpikeDiagnosticProperties spikeDiagnosticProperties;
	private final SpikeApplyMetricRecorder spikeApplyMetricRecorder;

	public SpikeApplyAppEdgeMetricsFilter(
		EventApplyRequestMatcher eventApplyRequestMatcher,
		SpikeDiagnosticProperties spikeDiagnosticProperties,
		SpikeApplyMetricRecorder spikeApplyMetricRecorder
	) {
		this.eventApplyRequestMatcher = eventApplyRequestMatcher;
		this.spikeDiagnosticProperties = spikeDiagnosticProperties;
		this.spikeApplyMetricRecorder = spikeApplyMetricRecorder;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		if (!eventApplyRequestMatcher.matches(request)) {
			filterChain.doFilter(request, response);
			return;
		}

		request.setAttribute(REQUEST_MATCHED_ATTRIBUTE, true);

		long start = System.nanoTime();
		String outcome = OUTCOME_SUCCESS;
		try {
			filterChain.doFilter(request, response);
			if (response.getStatus() >= 400) {
				outcome = OUTCOME_ERROR;
			}
		} catch (ServletException | IOException | RuntimeException exception) {
			outcome = OUTCOME_ERROR;
			throw exception;
		} finally {
			long totalNanos = System.nanoTime() - start;
			if (spikeDiagnosticProperties.isEnabled()) {
				restoreCorrelationMdc(request);
			}
			recordTimer(METRIC_APP_EDGE_TOTAL, outcome, totalNanos);

			Object requestTotalAttr = request.getAttribute(REQUEST_TOTAL_NANOS_ATTRIBUTE);
			if (requestTotalAttr instanceof Long requestTotalNanos) {
				long preRequestNanos = Math.max(0L, totalNanos - requestTotalNanos);
				recordTimer(METRIC_APP_EDGE_PRE_REQUEST_TOTAL, outcome, preRequestNanos);
			}
			if (spikeDiagnosticProperties.isEnabled()) {
				clearCorrelationMdc();
			}
		}
	}

	private void recordTimer(String metricName, String outcome, long durationNanos) {
		spikeApplyMetricRecorder.recordTimer(
			metricName,
			SpikeApplyRuntimeConfig.currentTxMode(),
			SpikeApplyRuntimeConfig.currentGate(),
			outcome,
			durationNanos
		);
	}

	private void restoreCorrelationMdc(HttpServletRequest request) {
		restoreIfPresent(request, TRACE_ID_ATTRIBUTE, "traceId");
		restoreIfPresent(request, USER_ID_ATTRIBUTE, "userId");
		restoreIfPresent(request, CLIENT_IP_ATTRIBUTE, "clientIp");
		restoreIfPresent(request, REQUEST_INFO_ATTRIBUTE, "requestInfo");
	}

	private void restoreIfPresent(HttpServletRequest request, String attributeName, String mdcKey) {
		Object value = request.getAttribute(attributeName);
		if (value instanceof String stringValue && !stringValue.isBlank()) {
			MDC.put(mdcKey, stringValue);
		}
	}

	private void clearCorrelationMdc() {
		MDC.remove("traceId");
		MDC.remove("userId");
		MDC.remove("clientIp");
		MDC.remove("requestInfo");
	}
}
