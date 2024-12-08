package org.sopt.makers.crew.main.internal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record UserOrgIdRequestDto(
	@Schema(example = "301", description = "플레이그라운드 유저 id")
	@NotNull
	Integer orgId
) {
}
