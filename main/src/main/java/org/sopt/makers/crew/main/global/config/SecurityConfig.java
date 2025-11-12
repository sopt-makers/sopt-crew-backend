package org.sopt.makers.crew.main.global.config;

import java.util.List;
import java.util.stream.Stream;

import org.sopt.makers.crew.main.global.security.filter.JwtAuthenticationExceptionFilter;
import org.sopt.makers.crew.main.global.security.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {
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
	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final JwtAuthenticationExceptionFilter jwtAuthenticationExceptionFilter;
	@Value("${management.endpoints.web.base-path}")
	private String actuatorEndPoint;
	@Value("${cors.allowed-origins.traffic}")
	private String trafficOrigin;

	private String[] getAuthWhitelist() {
		return new String[] {
			"/health",
			"/health/v2",
			"/meeting/v2/org-user/**",
			"/admin/v2/**",
			actuatorEndPoint + "/health",
			actuatorEndPoint + "/prometheus",
			actuatorEndPoint + "/caches/**",
			actuatorEndPoint + "/cachecontents/**",
			actuatorEndPoint + "/metrics/**",
			"/internal/**",
			"/slack/**"
		};
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable)
			.cors(Customizer.withDefaults())
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
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(jwtAuthenticationExceptionFilter, JwtAuthenticationFilter.class);
		return http.build();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowCredentials(false);

		configuration.setAllowedOrigins(List.of(
			"https://playground.sopt.org",
			"http://localhost:3000",
			"https://sopt-internal-dev.sopt.org",
			"https://crew.api.dev.sopt.org",
			"https://crew.api.prod.sopt.org",
			trafficOrigin
		));

		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("*"));

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);

		return source;
	}
}
