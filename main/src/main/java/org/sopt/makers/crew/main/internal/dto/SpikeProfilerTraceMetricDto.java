package org.sopt.makers.crew.main.internal.dto;

public record SpikeProfilerTraceMetricDto(
	String metricName,
	String kind,
	String outcome,
	double value,
	String unit
) {
}
