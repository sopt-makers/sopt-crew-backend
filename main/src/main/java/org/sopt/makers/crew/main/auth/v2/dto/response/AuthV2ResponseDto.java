package org.sopt.makers.crew.main.auth.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "인증 관련 response dto")
public record AuthV2ResponseDto(
	@Schema(example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjoi7Iah66-86recIiwiaWQiOjI4MywiaWF0IjoxNzI0MjYxMDg3LCJleHAiOjE3NjAyNjEwODd9.r2ScFqhSdt6pyl7gUvx0qFXHIknhtrXQVGjJavbAVRY", required = true, description = "크루 토큰")
	@NotNull
	String accessToken
) {
	public static AuthV2ResponseDto of(String accessToken){
		return new AuthV2ResponseDto(accessToken);
	}
}
