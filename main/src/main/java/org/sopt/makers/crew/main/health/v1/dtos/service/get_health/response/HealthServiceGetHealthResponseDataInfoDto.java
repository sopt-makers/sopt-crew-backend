package org.sopt.makers.crew.main.health.v1.dtos.service.get_health.response;

import static lombok.AccessLevel.*;

import java.util.Map;

import lombok.Builder;
import lombok.NoArgsConstructor;
import org.sopt.makers.crew.main.health.v1.enums.EnHealthV1ServiceType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "services의 key는 EnHealthV1ServiceType, value는 서비스 상태")
@NoArgsConstructor(access = PRIVATE)
public class HealthServiceGetHealthResponseDataInfoDto {
  /**
   * key는 서비스 타입, value는 서비스 상태
   * - Key로는 EnHealthV1ServiceType을 사용한다.
   * 
   * @see EnHealthV1ServiceType
   */
  private Map<EnHealthV1ServiceType, HealthServiceGetHealthResponseDataStatusDto> services;
  private HealthServiceGetHealthResponseDataStatusDto database;

  @Builder
  public HealthServiceGetHealthResponseDataInfoDto(
          Map<EnHealthV1ServiceType, HealthServiceGetHealthResponseDataStatusDto> services,
          HealthServiceGetHealthResponseDataStatusDto database) {
    this.services = services;
    this.database = database;
  }
}
