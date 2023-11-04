package org.sopt.makers.crew.main.health.v1.dtos.service.get_health.response;

import java.util.Map;

import org.sopt.makers.crew.main.health.v1.enums.EnHealthV1ServiceType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "services의 key는 EnHealthV1ServiceType, value는 서비스 상태")
public class HealthServiceGetHealthResponseDataDetailsDto {
  /**
   * key는 서비스 타입, value는 서비스 상태
   * - Key로는 EnHealthV1ServiceType을 사용한다.
   * 
   * @see EnHealthV1ServiceType
   */
  private Map<EnHealthV1ServiceType, HealthServiceGetHealthResponseDataStatusDto> services;
  private HealthServiceGetHealthResponseDataStatusDto database;
}
