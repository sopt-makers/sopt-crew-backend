package org.sopt.makers.crew.main.global.config;

import java.util.List;

import org.sopt.makers.crew.main.global.resolver.AuthenticatedUserIdArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

	private final AuthenticatedUserIdArgumentResolver authenticatedUserIdArgumentResolver;

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(authenticatedUserIdArgumentResolver);
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
			.allowedOrigins(
				"https://playground.sopt.org",
				"http://localhost:3000",
				"https://sopt-internal-dev.pages.dev",
				"https://crew.api.dev.sopt.org",
				"https://crew.api.prod.sopt.org"
			)
			.allowedMethods("GET", "POST", "PATCH", "DELETE", "PUT", "OPTIONS")
			.allowedHeaders("*")
			.allowCredentials(false);
	}
}
