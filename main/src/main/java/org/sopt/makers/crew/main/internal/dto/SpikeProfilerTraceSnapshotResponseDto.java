package org.sopt.makers.crew.main.internal.dto;

import java.util.List;

public record SpikeProfilerTraceSnapshotResponseDto(
	String generatedAt,
	List<SpikeProfilerTraceDto> traces
) {
}
