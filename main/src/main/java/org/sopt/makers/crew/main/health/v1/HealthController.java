package org.sopt.makers.crew.main.health.v1;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.crew.main.health.v1.dtos.service.get_health.response.HealthServiceGetHealthResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/health")
public class HealthController {

    private final HealthService healthService;

    @GetMapping("")
    public ResponseEntity<HealthServiceGetHealthResponseDto> getHealth() {
        return this.healthService.getHealth();
    }

    @GetMapping("/v2")
    public ResponseEntity<String> getHealthV2() {
        return ResponseEntity.ok("Spring Health Check Success!");
    }
}