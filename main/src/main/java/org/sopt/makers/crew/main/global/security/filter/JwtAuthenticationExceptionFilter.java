package org.sopt.makers.crew.main.global.security.filter;

import java.io.IOException;

import org.sopt.makers.crew.main.global.exception.BaseException;
import org.sopt.makers.crew.main.global.exception.ExceptionResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtAuthenticationExceptionFilter extends OncePerRequestFilter {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	protected void doFilterInternal(
		@NonNull final HttpServletRequest request,
		@NonNull final HttpServletResponse response,
		@NonNull final FilterChain filterChain)
		throws ServletException, IOException {
		try {
			filterChain.doFilter(request, response);
		} catch (BaseException e) {
			log.warn("JWT Authentication failed: {}", e.getMessage());
			handleBaseException(response, e);
		}
	}

	/**
	 * BaseException 처리 - 기존 ExceptionResponse 형식 사용
	 */
	private void handleBaseException(HttpServletResponse response, BaseException e) throws IOException {
		response.setContentType("application/json;charset=UTF-8");
		response.setStatus(e.getStatusCode());

		String jsonResponse = objectMapper.writeValueAsString(
			ExceptionResponse.fail(e.getErrorCode())
		);

		response.getWriter().write(jsonResponse);
	}
}
