package org.sopt.makers.crew.main.common.config;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.crew.main.common.config.jwt.JwtAuthenticationEntryPoint;
import org.sopt.makers.crew.main.common.config.jwt.JwtAuthenticationFilter;
import org.sopt.makers.crew.main.common.config.jwt.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

  private final JwtTokenProvider jwtTokenProvider;
  private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

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

  private static final String[] AUTH_WHITELIST = {
      "/health"
  };

  @Bean
  @Profile("dev")
  SecurityFilterChain devSecurityFilterChain(HttpSecurity http) throws Exception {
    return http.csrf((csrfConfig) ->
            csrfConfig.disable()
        )
        .sessionManagement((sessionManagement) ->
            sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .authorizeHttpRequests(
            authorize -> authorize.requestMatchers(AUTH_WHITELIST).permitAll()
                .requestMatchers(SWAGGER_URL).permitAll()
                .anyRequest().authenticated()
        )
        .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, jwtAuthenticationEntryPoint),
            UsernamePasswordAuthenticationFilter.class)
        .exceptionHandling(exceptionHandling ->
            exceptionHandling.authenticationEntryPoint(jwtAuthenticationEntryPoint)
        )
        .build();
  }

}