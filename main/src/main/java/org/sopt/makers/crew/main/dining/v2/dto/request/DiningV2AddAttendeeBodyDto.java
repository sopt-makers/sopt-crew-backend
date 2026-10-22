package org.sopt.makers.crew.main.dining.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DiningV2AddAttendeeBodyDto(
	@Schema(description = "추가할 참석자의 크루 사용자 ID", example = "1")
	@NotNull @Positive Integer userId
) {
}
