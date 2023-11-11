package org.sopt.makers.crew.main.health.v1;

import org.sopt.makers.crew.main.health.v1.dtos.service.get_health.response.HealthServiceGetHealthResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class HealthService {

  private final WebClient webClient;

  public HealthService(WebClient.Builder webClientBuilder) {
    this.webClient = webClientBuilder.baseUrl("http://nestjs:3000").build();
  }

  public ResponseEntity<HealthServiceGetHealthResponseDto> getHealth() {
    return this.webClient.get().uri("/").retrieve().toEntity(HealthServiceGetHealthResponseDto.class).block();
  }
}
