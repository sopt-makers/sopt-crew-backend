package org.sopt.makers.crew.main.health.v1;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.makers.crew.main.health.v1.dtos.service.get_health.response.HealthServiceGetHealthResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/health")
@Slf4j
public class HealthController {

  private final HealthService healthService;

  @GetMapping("")
  public ResponseEntity<HealthServiceGetHealthResponseDto> getHealth() {
    log.info("health 컨트롤러 come in V1");
    return this.healthService.getHealth();
  }

  @GetMapping("/v2")
  public ResponseEntity<String> getHealthV2() {
    log.info("health 컨트롤러 come in V2");
    return ResponseEntity.ok().body("이건 내가 임의로 만든 헬스체크");
  }
}