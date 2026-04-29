package org.sopt.makers.crew.main.global.config.lambda;

import java.io.IOException;

import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Profile("lambda-dev")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LambdaRequestLoggingFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {
		log.info(
			"lambda servlet request method={} uri={} query={}",
			request.getMethod(),
			request.getRequestURI(),
			request.getQueryString()
		);

		filterChain.doFilter(request, response);

		log.info(
			"lambda servlet response method={} uri={} status={} committed={}",
			request.getMethod(),
			request.getRequestURI(),
			response.getStatus(),
			response.isCommitted()
		);
	}
}
