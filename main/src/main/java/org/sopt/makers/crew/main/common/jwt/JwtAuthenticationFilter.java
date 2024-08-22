package org.sopt.makers.crew.main.common.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import lombok.RequiredArgsConstructor;

import org.sopt.makers.crew.main.common.response.ErrorStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider jwtTokenProvider;
	private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain chain) throws ServletException, IOException {
		String accessToken = jwtTokenProvider.resolveToken(request);

		if (accessToken != null) {
			// 토큰 검증
			if (jwtTokenProvider.validateToken(accessToken)
				== JwtExceptionType.VALID_JWT_TOKEN) {     // 토큰이 존재하고 유효한 토큰일 때만
				Integer userId = jwtTokenProvider.getAccessTokenPayload(accessToken);
				UserAuthentication authentication = new UserAuthentication(userId, null,
					null);       //사용자 객체 생성
				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(
					request));  // request 정보로 사용자 객체 디테일 설정
				SecurityContextHolder.getContext().setAuthentication(authentication);
			} else {
				jwtAuthenticationEntryPoint.commence(request, response,
					new AuthenticationException(ErrorStatus.UNAUTHORIZED_TOKEN.getErrorCode()) {
					});
				return;
			}
		}
		chain.doFilter(request, response);
	}
}
