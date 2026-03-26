package org.sopt.makers.crew.main.global.security.filter;

import static org.sopt.makers.crew.main.global.metrics.SpikeApplyMetrics.OUTCOME_ERROR;
import static org.sopt.makers.crew.main.global.metrics.SpikeApplyMetrics.OUTCOME_SUCCESS;
import static org.sopt.makers.crew.main.global.metrics.SpikeApplyMetrics.USER_ID_ATTRIBUTE;

import java.io.IOException;

import org.slf4j.MDC;
import org.sopt.makers.crew.main.global.jwt.authenticator.JwtAuthenticator;
import org.sopt.makers.crew.main.global.metrics.SpikeApplyRequestSupport;
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

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private static final String ACCESS_TOKEN_PREFIX = "Bearer ";
	private static final String USER_ID = "userId";
	private final SpikeApplyRequestSupport spikeApplyRequestSupport;
	private final JwtAuthenticator jwtAuthenticator;

	@Override
	protected void doFilterInternal(
		@NonNull final HttpServletRequest request,
		@NonNull final HttpServletResponse response,
		@NonNull final FilterChain filterChain)
		throws ServletException, IOException {
		String authorizationToken = getAuthorizationToken(request);
		if (!StringUtils.hasText(authorizationToken)) {
			filterChain.doFilter(request, response);
			return;
		}

		boolean persistDiagnosticAttributes = spikeApplyRequestSupport.shouldPersistDiagnosticAttributes(request);
		String outcome = OUTCOME_SUCCESS;
		try {
			MakersAuthentication authentication = jwtAuthenticator.authenticate(authorizationToken);
			authentication.setAuthenticated(true);
			SecurityContextHolder.getContext().setAuthentication(authentication);
			String authenticatedUserId = String.valueOf(authentication.getPrincipal());
			MDC.put(USER_ID, authenticatedUserId);
			if (persistDiagnosticAttributes) {
				request.setAttribute(USER_ID_ATTRIBUTE, authenticatedUserId);
			}
		} catch (RuntimeException exception) {
			outcome = OUTCOME_ERROR;
			throw exception;
		} finally {
			// EXP: operational-no-observer narrow p95
		}
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
