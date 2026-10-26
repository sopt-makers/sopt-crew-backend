package org.sopt.makers.crew.main.dining.v2.dto.response;

import org.sopt.makers.crew.main.entity.dining.enums.DiningPayerStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "회차 참석 현황")
public record DiningV2GetAttendanceResponseDto(@NotNull long attendeeCount, @NotNull boolean isAttending,
	@Schema(description = "본인의 해당 회차 공동 결제자 상태. 미참석 또는 요청 없음이면 null")
	DiningPayerStatus payerStatus) {
}
