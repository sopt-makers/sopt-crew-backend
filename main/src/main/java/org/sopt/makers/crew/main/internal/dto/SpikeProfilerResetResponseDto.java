package org.sopt.makers.crew.main.internal.dto;

public record SpikeProfilerResetResponseDto(
	String resetAt,
	int clearedTimers,
	int clearedSummaries,
	int clearedTraces
) {
}
