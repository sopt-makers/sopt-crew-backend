package org.sopt.makers.crew.main.health.v1.dtos.service.get_health.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HealthServiceGetHealthResponseDto {

  private int statusCode;
  private HealthServiceGetHealthResponseDataDto data;
}
