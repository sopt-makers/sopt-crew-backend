package org.sopt.makers.crew.main.internal.dto;

public record SpikeProfilerSummaryMetricDto(
	String metricName,
	String txMode,
	String gate,
	String outcome,
	long count,
	double mean,
	double min,
	double max,
	double p50,
	double p90,
	double p95,
	double p99
) {
}
