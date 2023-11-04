package org.sopt.makers.crew.main.health.v1;

import org.sopt.makers.crew.main.health.v1.dtos.service.get_health.response.HealthServiceGetHealthResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
  private final HealthService healthService;

  @Autowired
  public HealthController(HealthService healthService) {
    this.healthService = healthService;
  }

  @GetMapping("/")
  public ResponseEntity<HealthServiceGetHealthResponseDto> getHealth() {
    return this.healthService.getHealth();
  }
}