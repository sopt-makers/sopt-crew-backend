package org.sopt.makers.crew.main.health.v1.dtos.service.get_health.response;

import static lombok.AccessLevel.*;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = PRIVATE)
public class HealthServiceGetHealthResponseDto {

  private int statusCode;
  private HealthServiceGetHealthResponseDataDto data;

  @Builder
  public HealthServiceGetHealthResponseDto(int statusCode, HealthServiceGetHealthResponseDataDto data) {
    this.statusCode = statusCode;
    this.data = data;
  }
}
