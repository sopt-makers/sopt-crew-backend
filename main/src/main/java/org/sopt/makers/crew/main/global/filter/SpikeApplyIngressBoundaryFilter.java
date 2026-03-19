package org.sopt.makers.crew.main.global.filter;

import static org.sopt.makers.crew.main.global.metrics.SpikeApplyMetrics.INGRESS_START_NANOS_ATTRIBUTE;

import java.io.IOException;

import org.sopt.makers.crew.main.global.metrics.SpikeApplyRequestSupport;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SpikeApplyIngressBoundaryFilter extends OncePerRequestFilter {
	private final SpikeApplyRequestSupport spikeApplyRequestSupport;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		if (spikeApplyRequestSupport.isSpikeApplyRequest(request)
			&& request.getAttribute(INGRESS_START_NANOS_ATTRIBUTE) == null) {
			request.setAttribute(INGRESS_START_NANOS_ATTRIBUTE, System.nanoTime());
		}
		filterChain.doFilter(request, response);
	}
}
