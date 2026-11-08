package org.sopt.makers.crew.main.dining.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "회차 전체 참석자 CSV 다운로드 URL")
public record DiningV2GetAttendeesCsvResponseDto(
	@Schema(description = "S3에 업로드된 CSV 파일 URL") @NotNull String url
) {
}
