package org.sopt.makers.crew.main.health.v1.dtos.service.get_health.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HealthServiceGetHealthResponseDataDto {
  private String status;
  private HealthServiceGetHealthResponseDataInfoDto info;
  private HealthServiceGetHealthResponseDataErrorDto error;
  private HealthServiceGetHealthResponseDataDetailsDto details;

  // getters and setters
}
