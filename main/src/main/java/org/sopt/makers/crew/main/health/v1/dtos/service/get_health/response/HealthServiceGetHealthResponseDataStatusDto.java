package org.sopt.makers.crew.main.health.v1.dtos.service.get_health.response;

import static lombok.AccessLevel.PRIVATE;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = PRIVATE)
public class HealthServiceGetHealthResponseDataStatusDto {
  private String status;

  @Builder
  public HealthServiceGetHealthResponseDataStatusDto(String status) {
    this.status = status;
  }
}
