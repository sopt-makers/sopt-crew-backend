package org.sopt.makers.crew.main.global.jwt;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.sopt.makers.crew.main.global.exception.ExceptionResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException authException) throws IOException {
		setResponse(response);
	}

	public void setResponse(HttpServletResponse response) throws IOException {
		response.setContentType("application/json;charset=UTF-8");
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

		ObjectMapper mapper = new ObjectMapper();
		String jsonResponse = mapper.writeValueAsString(ExceptionResponse.fail(INVALID_INPUT_VALUE_FILTER.getErrorCode()));

		response.getWriter().write(jsonResponse);
	}

}
