package org.sopt.makers.crew.main.global.config;

import java.util.Arrays;
import java.util.stream.Stream;

import org.sopt.makers.crew.main.global.jwt.JwtAuthenticationEntryPoint;
import org.sopt.makers.crew.main.global.jwt.JwtAuthenticationFilter;
import org.sopt.makers.crew.main.global.jwt.JwtTokenProvider;
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
	private final JwtTokenProvider jwtTokenProvider;
	private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
	@Value("${management.endpoints.web.base-path}")
	private String actuatorEndPoint;

	private String[] getAuthWhitelist() {
		return new String[] {
			"/health",
			"/health/v2",
			"/meeting/v2/org-user/**",
			"/auth/v2",
			"/auth/v2/**",
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
			.cors(Customizer.withDefaults())
			.sessionManagement(
				(sessionManagement) -> sessionManagement.sessionCreationPolicy(
					SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(
				authorize -> authorize
					.requestMatchers(Stream
						.of(SWAGGER_URL)
						.map(AntPathRequestMatcher::antMatcher)
						.toArray(AntPathRequestMatcher[]::new)).permitAll()
					.requestMatchers(Stream
						.of(getAuthWhitelist())
						.map(AntPathRequestMatcher::antMatcher)
						.toArray(AntPathRequestMatcher[]::new)).permitAll()
					.anyRequest().authenticated())
			.addFilterBefore(
				new JwtAuthenticationFilter(this.jwtTokenProvider,
					this.jwtAuthenticationEntryPoint),
				UsernamePasswordAuthenticationFilter.class)
			.exceptionHandling(exceptionHandling -> exceptionHandling
				.authenticationEntryPoint(this.jwtAuthenticationEntryPoint));
		return http.build();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(
			Arrays.asList(
				"https://playground.sopt.org/",
				"http://localhost:3000/",
				"https://sopt-internal-dev.pages.dev/",
				"https://crew.api.dev.sopt.org",
				"https://crew.api.prod.sopt.org"
			));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PATCH", "DELETE", "PUT", "OPTIONS"));
		configuration.addAllowedHeader("*");
		configuration.setAllowCredentials(false);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

}
