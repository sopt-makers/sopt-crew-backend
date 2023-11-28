package org.sopt.makers.crew.main.health.v1.dtos.service.get_health.response;

import static lombok.AccessLevel.PRIVATE;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = PRIVATE)
public class HealthServiceGetHealthResponseDataDto {
  private String status;
  private HealthServiceGetHealthResponseDataInfoDto info;
  private HealthServiceGetHealthResponseDataInfoDto error;
  private HealthServiceGetHealthResponseDataInfoDto details;

  @Builder
  public HealthServiceGetHealthResponseDataDto(String status, HealthServiceGetHealthResponseDataInfoDto info,
                                               HealthServiceGetHealthResponseDataInfoDto error,
                                               HealthServiceGetHealthResponseDataInfoDto details) {
    this.status = status;
    this.info = info;
    this.error = error;
    this.details = details;
  }
}
