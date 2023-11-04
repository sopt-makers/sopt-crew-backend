package org.sopt.makers.crew.main.health.v1.enums;

public enum EnHealthV1ServiceType {
  CREW_WEB_DEV("crew-web-dev"),
  CREW_WEB_PROD("crew-web-prod");

  private final String value;

  EnHealthV1ServiceType(String value) {
    this.value = value;
  }

  public String getValue() {
    return this.value;
  }
}
