package org.sopt.makers.crew.main.internal.dto;

import java.util.List;

public record SpikeProfilerTraceDto(
	String traceId,
	String requestInfo,
	String userId,
	String startedAt,
	String lastUpdatedAt,
	String completedAt,
	String finalOutcome,
	List<SpikeProfilerTraceMetricDto> metrics
) {
}
