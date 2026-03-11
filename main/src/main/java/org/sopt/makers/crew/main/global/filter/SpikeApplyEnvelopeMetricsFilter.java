package org.sopt.makers.crew.main.global.filter;

import static org.sopt.makers.crew.main.global.metrics.SpikeApplyMetrics.MDC_ACTIVE_KEY;
import static org.sopt.makers.crew.main.global.metrics.SpikeApplyMetrics.MDC_ACTIVE_PREVIOUS;
import static org.sopt.makers.crew.main.global.metrics.SpikeApplyMetrics.METRIC_REQUEST_TOTAL;
import static org.sopt.makers.crew.main.global.metrics.SpikeApplyMetrics.OUTCOME_ERROR;
import static org.sopt.makers.crew.main.global.metrics.SpikeApplyMetrics.OUTCOME_SUCCESS;
import static org.sopt.makers.crew.main.global.metrics.SpikeApplyMetrics.REQUEST_MATCHED_ATTRIBUTE;

import java.io.IOException;

import org.slf4j.MDC;
import org.sopt.makers.crew.main.global.metrics.EventApplyRequestMatcher;
import org.sopt.makers.crew.main.global.metrics.SpikeApplyMetricRecorder;
import org.sopt.makers.crew.main.global.metrics.SpikeApplyRuntimeConfig;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class SpikeApplyEnvelopeMetricsFilter extends OncePerRequestFilter {
	private final EventApplyRequestMatcher eventApplyRequestMatcher;
	private final SpikeApplyMetricRecorder spikeApplyMetricRecorder;

	public SpikeApplyEnvelopeMetricsFilter(
		EventApplyRequestMatcher eventApplyRequestMatcher,
		SpikeApplyMetricRecorder spikeApplyMetricRecorder
	) {
		this.eventApplyRequestMatcher = eventApplyRequestMatcher;
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
		request.setAttribute(MDC_ACTIVE_PREVIOUS, MDC.get(MDC_ACTIVE_KEY));
		MDC.put(MDC_ACTIVE_KEY, "true");

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
			spikeApplyMetricRecorder.recordTimer(
				METRIC_REQUEST_TOTAL,
				SpikeApplyRuntimeConfig.currentTxMode(),
				SpikeApplyRuntimeConfig.currentGate(),
				outcome,
				System.nanoTime() - start
			);
			restoreMdcActive(request);
		}
	}

	private void restoreMdcActive(HttpServletRequest request) {
		String previousValue = (String)request.getAttribute(MDC_ACTIVE_PREVIOUS);
		if (previousValue == null) {
			MDC.remove(MDC_ACTIVE_KEY);
			return;
		}
		MDC.put(MDC_ACTIVE_KEY, previousValue);
	}
}
