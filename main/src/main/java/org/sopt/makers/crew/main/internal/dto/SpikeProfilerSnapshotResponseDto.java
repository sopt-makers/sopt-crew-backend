package org.sopt.makers.crew.main.internal.dto;

import java.util.List;

public record SpikeProfilerSnapshotResponseDto(
	String generatedAt,
	List<SpikeProfilerTimerMetricDto> timers,
	List<SpikeProfilerSummaryMetricDto> summaries
) {
}
