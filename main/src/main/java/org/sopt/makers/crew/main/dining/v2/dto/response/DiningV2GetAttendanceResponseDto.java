package org.sopt.makers.crew.main.dining.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회차 참석 현황")
public record DiningV2GetAttendanceResponseDto(long attendeeCount, boolean isAttending) {
}
