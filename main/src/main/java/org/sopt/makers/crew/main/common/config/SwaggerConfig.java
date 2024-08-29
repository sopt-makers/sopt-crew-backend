package org.sopt.makers.crew.main.common.config;

import static io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(servers = {@Server(url = "/", description = "Generated Server URL")})
public class SwaggerConfig {

	@Bean
	public OpenAPI openAPI() {

		Info info = new Info().title("Crew API 문서").version("1.1.2")
			.description("Crew API 문서");

		String jwtSchemeName = "JWT Authorization";
		SecurityRequirement securityRequirement =
			new SecurityRequirement().addList(jwtSchemeName);

		Components components = new Components().addSecuritySchemes(jwtSchemeName,
			new SecurityScheme().name(jwtSchemeName).type(HTTP) // HTTP 방식
				.scheme("bearer").bearerFormat("JWT"));

		return new OpenAPI().info(info).addSecurityItem(securityRequirement)
			.components(components);
	}
}
