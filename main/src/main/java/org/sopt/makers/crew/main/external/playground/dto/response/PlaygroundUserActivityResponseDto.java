package org.sopt.makers.crew.main.external.playground.dto.response;

public record PlaygroundUserActivityResponseDto(
	Long id,
	int generation,
	String part,
	String team
) {
}
