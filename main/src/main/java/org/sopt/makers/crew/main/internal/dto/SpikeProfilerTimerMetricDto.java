package org.sopt.makers.crew.main.internal.dto;

public record SpikeProfilerTimerMetricDto(
	String metricName,
	String txMode,
	String gate,
	String outcome,
	long count,
	double totalMs,
	double meanMs,
	double minMs,
	double maxMs,
	double p50Ms,
	double p90Ms,
	double p95Ms,
	double p99Ms
) {
}
