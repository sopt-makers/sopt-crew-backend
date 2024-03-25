package org.sopt.makers.crew.main.common.config;

import java.util.Arrays;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.crew.main.common.config.jwt.JwtAuthenticationEntryPoint;
import org.sopt.makers.crew.main.common.config.jwt.JwtAuthenticationFilter;
import org.sopt.makers.crew.main.common.config.jwt.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

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
      "/health",
      "/meeting/v2/org-user/**"
  };

  @Bean
  @Profile("dev")
  SecurityFilterChain devSecurityFilterChain(HttpSecurity http) throws Exception {
    http.csrf((csrfConfig) -> csrfConfig.disable())
        .cors(Customizer.withDefaults())
        .sessionManagement(
            (sessionManagement) -> sessionManagement.sessionCreationPolicy(
                SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            authorize -> authorize.requestMatchers(Stream
                            .of(SWAGGER_URL)
                            .map(AntPathRequestMatcher::antMatcher)
                            .toArray(AntPathRequestMatcher[]::new)).permitAll()
                .requestMatchers(Stream
                        .of(AUTH_WHITELIST)
                        .map(AntPathRequestMatcher::antMatcher)
                        .toArray(AntPathRequestMatcher[]::new)).permitAll()
                .anyRequest().authenticated())
        .addFilterBefore(
            new JwtAuthenticationFilter(this.jwtTokenProvider, this.jwtAuthenticationEntryPoint),
            UsernamePasswordAuthenticationFilter.class)
        .exceptionHandling(exceptionHandling -> exceptionHandling
            .authenticationEntryPoint(this.jwtAuthenticationEntryPoint));
    return http.build();
  }

  @Bean
  @Profile("test")
  SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
    http.csrf((csrfConfig) -> csrfConfig.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(
                    (sessionManagement) -> sessionManagement.sessionCreationPolicy(
                            SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(
                    authorize -> authorize.requestMatchers(Stream
                                    .of(SWAGGER_URL)
                                    .map(AntPathRequestMatcher::antMatcher)
                                    .toArray(AntPathRequestMatcher[]::new)).permitAll()
                            .requestMatchers(Stream
                                    .of(AUTH_WHITELIST)
                                    .map(AntPathRequestMatcher::antMatcher)
                                    .toArray(AntPathRequestMatcher[]::new)).permitAll()
                            .anyRequest().authenticated())
            .addFilterBefore(
                    new JwtAuthenticationFilter(this.jwtTokenProvider, this.jwtAuthenticationEntryPoint),
                    UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(exceptionHandling -> exceptionHandling
                    .authenticationEntryPoint(this.jwtAuthenticationEntryPoint));
    return http.build();
  }

  @Bean
  @Profile("prod")
  SecurityFilterChain prodSecurityFilterChain(HttpSecurity http) throws Exception {
    http.csrf((csrfConfig) -> csrfConfig.disable())
        .cors(Customizer.withDefaults())
        .sessionManagement(
            (sessionManagement) -> sessionManagement.sessionCreationPolicy(
                SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
                authorize -> authorize.requestMatchers(Stream
                                .of(SWAGGER_URL)
                                .map(AntPathRequestMatcher::antMatcher)
                                .toArray(AntPathRequestMatcher[]::new)).permitAll()
                        .requestMatchers(Stream
                                .of(AUTH_WHITELIST)
                                .map(AntPathRequestMatcher::antMatcher)
                                .toArray(AntPathRequestMatcher[]::new)).permitAll()
                        .anyRequest().authenticated())
        .addFilterBefore(
            new JwtAuthenticationFilter(this.jwtTokenProvider, this.jwtAuthenticationEntryPoint),
            UsernamePasswordAuthenticationFilter.class)
        .exceptionHandling(exceptionHandling -> exceptionHandling
            .authenticationEntryPoint(this.jwtAuthenticationEntryPoint));
    return http.build();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(
        Arrays.asList("https://playground.sopt.org/", "http://localhost:3000/",
            "https://sopt-internal-dev.pages.dev/"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PATCH", "DELETE", "OPTIONS"));
    configuration.addAllowedHeader("*");
    configuration.setAllowCredentials(false);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

}
