package org.sopt.makers.crew.main.auth.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "인증 관련 request body dto")
public record AuthV2RequestDto(
	@Schema(example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxOCIsImV4cCI6MTY3OTYwOTk3OH0.9D_Tc14J3S0VDmQgT5lUJ5i3KJZob3NKVmSS3fPjHAo", required = true, description = "인증 중앙화 토큰")
	@NotNull
	String authToken
) {
}
