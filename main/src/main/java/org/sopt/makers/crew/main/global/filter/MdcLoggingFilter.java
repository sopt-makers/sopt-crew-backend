package org.sopt.makers.crew.main.global.filter;

import static org.sopt.makers.crew.main.global.metrics.SpikeApplyMetrics.CLIENT_IP_ATTRIBUTE;
import static org.sopt.makers.crew.main.global.metrics.SpikeApplyMetrics.REQUEST_INFO_ATTRIBUTE;
import static org.sopt.makers.crew.main.global.metrics.SpikeApplyMetrics.TRACE_ID_ATTRIBUTE;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
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
public class MdcLoggingFilter extends OncePerRequestFilter {
	private static final String TRACE_ID = "traceId";
	private static final String CLIENT_IP = "clientIp";
	private static final String REQUEST_INFO = "requestInfo";
	private static final String X_CORRELATION_ID = "X-Correlation-ID";
	private static final String X_REQUEST_ID = "X-Request-ID";
	private static final String X_FORWARDED_FOR = "X-Forwarded-For";
	private static final String X_REAL_IP = "X-Real-IP";
	private final SpikeApplyRequestSupport spikeApplyRequestSupport;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		try {
			boolean persistDiagnosticAttributes = spikeApplyRequestSupport.shouldPersistDiagnosticAttributes(request);
			String traceId = resolveTraceId(request);
			String clientIp = spikeApplyRequestSupport.shouldCaptureClientIp(request) ? resolveClientIp(request) : null;
			String requestInfo = request.getMethod() + " " + request.getRequestURI();
			MDC.put(TRACE_ID, traceId);
			if (clientIp != null) {
				MDC.put(CLIENT_IP, clientIp);
				if (persistDiagnosticAttributes) {
					request.setAttribute(CLIENT_IP_ATTRIBUTE, clientIp);
				}
			}
			MDC.put(REQUEST_INFO, requestInfo);
			if (persistDiagnosticAttributes) {
				request.setAttribute(TRACE_ID_ATTRIBUTE, traceId);
				request.setAttribute(REQUEST_INFO_ATTRIBUTE, requestInfo);
			}
			response.setHeader(X_CORRELATION_ID, traceId);
			response.setHeader(X_REQUEST_ID, traceId);
			filterChain.doFilter(request, response);
		} finally {
			MDC.clear();
		}
	}

	private String resolveTraceId(HttpServletRequest request) {
		String correlationId = request.getHeader(X_CORRELATION_ID);
		if (correlationId != null && !correlationId.isBlank()) {
			return correlationId;
		}
		String requestId = request.getHeader(X_REQUEST_ID);
		return (requestId != null && !requestId.isBlank()) ? requestId : UUID.randomUUID().toString();
	}

	private String resolveClientIp(HttpServletRequest request) {
		String ip = request.getHeader(X_FORWARDED_FOR);
		if (ip != null && !ip.isBlank()) {
			return ip.split(",")[0].trim();
		}
		ip = request.getHeader(X_REAL_IP);
		if (ip != null && !ip.isBlank()) {
			return ip;
		}
		return request.getRemoteAddr();
	}

}
