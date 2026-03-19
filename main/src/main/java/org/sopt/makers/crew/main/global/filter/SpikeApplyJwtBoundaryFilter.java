package org.sopt.makers.crew.main.global.filter;

import static org.sopt.makers.crew.main.global.metrics.SpikeApplyMetrics.METRIC_JWT_BOUNDARY_TOTAL;
import static org.sopt.makers.crew.main.global.metrics.SpikeApplyMetrics.OUTCOME_ERROR;
import static org.sopt.makers.crew.main.global.metrics.SpikeApplyMetrics.OUTCOME_SUCCESS;

import java.io.IOException;

import org.sopt.makers.crew.main.global.metrics.SpikeApplyMetricRecorder;
import org.sopt.makers.crew.main.global.metrics.SpikeApplyRequestSupport;
import org.sopt.makers.crew.main.global.metrics.SpikeApplyRuntimeConfig;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SpikeApplyJwtBoundaryFilter extends OncePerRequestFilter {
	private final SpikeApplyRequestSupport spikeApplyRequestSupport;
	private final SpikeApplyMetricRecorder spikeApplyMetricRecorder;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		if (!spikeApplyRequestSupport.isSpikeApplyRequest(request)) {
			filterChain.doFilter(request, response);
			return;
		}

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
				METRIC_JWT_BOUNDARY_TOTAL,
				SpikeApplyRuntimeConfig.currentTxMode(),
				SpikeApplyRuntimeConfig.currentGate(),
				outcome,
				System.nanoTime() - start
			);
		}
	}
}
