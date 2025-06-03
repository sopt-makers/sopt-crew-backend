package org.sopt.makers.crew.main.global.config;

import java.util.stream.Stream;

import org.sopt.makers.crew.main.global.security.filter.JwtAuthenticationExceptionFilter;
import org.sopt.makers.crew.main.global.security.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {
	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final JwtAuthenticationExceptionFilter jwtAuthenticationExceptionFilter;

	@Value("${management.endpoints.web.base-path}")
	private String actuatorEndPoint;

	private static final String[] SWAGGER_URL = {
		"/swagger-resources/**",
		"/favicon.ico",
		"/api-docs/**",
		"/swagger-ui/**",
		"/swagger-ui.html",
		"/swagger-ui/index.html",
		"/docs/swagger-ui/index.html",
		"/swagger-ui/swagger-ui.css",
	};

	private String[] getAuthWhitelist() {
		return new String[] {
			"/health",
			"/health/v2",
			"/meeting/v2/org-user/**",
			"/auth/v2",
			"/auth/v2/**",
			"/admin/v2/**",
			actuatorEndPoint + "/health",
			actuatorEndPoint + "/prometheus",
			actuatorEndPoint + "/caches/**",
			actuatorEndPoint + "/cachecontents/**",
			actuatorEndPoint + "/metrics/**",
			"/internal/**"
		};
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable)
			.cors(AbstractHttpConfigurer::disable)
			.sessionManagement(
				sessionManagement -> sessionManagement
					.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(
				authorize -> authorize
					.requestMatchers(Stream.of(SWAGGER_URL)
						.map(AntPathRequestMatcher::antMatcher)
						.toArray(AntPathRequestMatcher[]::new)).permitAll()
					.requestMatchers(Stream.of(getAuthWhitelist())
						.map(AntPathRequestMatcher::antMatcher)
						.toArray(AntPathRequestMatcher[]::new)).permitAll()
					.anyRequest().authenticated())
			.addFilterBefore(jwtAuthenticationExceptionFilter, UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(jwtAuthenticationFilter, JwtAuthenticationExceptionFilter.class);
		return http.build();
	}
}
