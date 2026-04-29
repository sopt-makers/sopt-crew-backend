package org.sopt.makers.crew.main.global.security.filter;

import java.io.IOException;

import org.sopt.makers.crew.main.global.jwt.authenticator.JwtAuthenticator;
import org.sopt.makers.crew.main.global.security.authentication.MakersAuthentication;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private static final String ACCESS_TOKEN_PREFIX = "Bearer ";
	private final JwtAuthenticator jwtAuthenticator;

	@Override
	protected void doFilterInternal(
		@NonNull final HttpServletRequest request,
		@NonNull final HttpServletResponse response,
		@NonNull final FilterChain filterChain)
		throws ServletException, IOException {
		String authorizationToken = getAuthorizationToken(request);
		if (!StringUtils.hasText(authorizationToken)) {
			log.info("jwt authentication skipped method={} uri={} reason=missing_authorization",
				request.getMethod(), request.getRequestURI());
			filterChain.doFilter(request, response);
			return;
		}

		MakersAuthentication authentication = jwtAuthenticator.authenticate(authorizationToken);
		authentication.setAuthenticated(true);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		log.info("jwt authentication success method={} uri={} userId={} roles={}",
			request.getMethod(), request.getRequestURI(), authentication.getUserId(), authentication.getRoles());
		filterChain.doFilter(request, response);
	}

	private String getAuthorizationToken(final HttpServletRequest request) {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);

		if (header == null || !header.startsWith(ACCESS_TOKEN_PREFIX)) {
			return null;

		}
		return header.substring(ACCESS_TOKEN_PREFIX.length()).trim();
	}
}
