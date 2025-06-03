package org.sopt.makers.crew.main.auth.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "인증 관련 response dto")
public record AuthV2ResponseDto(
	@Schema(example = "1", required = true, description = "")
	@NotNull
	Integer userId
) {
	public static AuthV2ResponseDto from(Integer id) {
		return new AuthV2ResponseDto(id);
	}
}
