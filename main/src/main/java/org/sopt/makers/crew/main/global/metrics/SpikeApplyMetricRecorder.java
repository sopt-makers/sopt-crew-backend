package org.sopt.makers.crew.main.global.metrics;

public interface SpikeApplyMetricRecorder {
	void recordTimer(String metricName, String txMode, String gate, String outcome, long nanos);

	void recordSummary(String metricName, String txMode, String gate, String outcome, double value);
}
