package org.sopt.makers.crew.main.global.filter;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class MdcLoggingFilter extends OncePerRequestFilter {
	private static final String TRACE_ID = "traceId";
	private static final String CLIENT_IP = "clientIp";
	private static final String REQUEST_INFO = "requestInfo";
	private static final String X_REQUEST_ID = "X-Request-ID";
	private static final String X_FORWARDED_FOR = "X-Forwarded-For";
	private static final String X_REAL_IP = "X-Real-IP";

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		try {
			String traceId = resolveTraceId(request);
			MDC.put(TRACE_ID, traceId);
			MDC.put(CLIENT_IP, resolveClientIp(request));
			MDC.put(REQUEST_INFO, request.getMethod() + " " + request.getRequestURI());
			response.setHeader(X_REQUEST_ID, traceId);
			filterChain.doFilter(request, response);
		} finally {
			MDC.clear();
		}
	}

	private String resolveTraceId(HttpServletRequest request) {
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
