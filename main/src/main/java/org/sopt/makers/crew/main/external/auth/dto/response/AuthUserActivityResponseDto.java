package org.sopt.makers.crew.main.external.auth.dto.response;

public record AuthUserActivityResponseDto(
	Long activityId,
	int generation,
	String part,
	String team
) {
}
