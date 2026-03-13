package org.sopt.makers.crew.main.meeting.v2.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.sopt.makers.crew.main.global.metrics.SpikeApplyMetrics;
import org.sopt.makers.crew.main.global.metrics.SpikeDiagnosticProperties;
import org.sopt.makers.crew.main.internal.dto.SpikeProfilerResetResponseDto;
import org.sopt.makers.crew.main.internal.dto.SpikeProfilerSnapshotResponseDto;
import org.sopt.makers.crew.main.internal.dto.SpikeProfilerTraceDto;
import org.sopt.makers.crew.main.internal.dto.SpikeProfilerTraceSnapshotResponseDto;
import org.sopt.makers.crew.main.internal.dto.SpikeProfilerTimerMetricDto;

class SpikeApplyProfilerTest {

	private final SpikeApplyProfiler spikeApplyProfiler = new SpikeApplyProfiler(new SpikeDiagnosticProperties());

	@Test
	void snapshot_타이머_통계를_반환한다() {
		// given
		spikeApplyProfiler.recordTimer("crew.spike.apply.business.test", "fat", "on", "success", 10_000_000L);
		spikeApplyProfiler.recordTimer("crew.spike.apply.business.test", "fat", "on", "success", 20_000_000L);
		spikeApplyProfiler.recordTimer("crew.spike.apply.business.test", "fat", "on", "success", 30_000_000L);
		spikeApplyProfiler.recordTimer("crew.spike.apply.business.test", "fat", "on", "success", 40_000_000L);

		// when
		SpikeProfilerSnapshotResponseDto snapshot = spikeApplyProfiler.snapshot();

		// then
		assertThat(snapshot.timers()).hasSize(1);
		SpikeProfilerTimerMetricDto metric = snapshot.timers().get(0);
		assertThat(metric.metricName()).isEqualTo("crew.spike.apply.business.test");
		assertThat(metric.count()).isEqualTo(4);
		assertThat(metric.totalMs()).isEqualTo(100.0);
		assertThat(metric.meanMs()).isEqualTo(25.0);
		assertThat(metric.minMs()).isEqualTo(10.0);
		assertThat(metric.maxMs()).isEqualTo(40.0);
		assertThat(metric.p95Ms()).isEqualTo(40.0);
	}

	@Test
	void reset_누적값을_초기화한다() {
		// given
		spikeApplyProfiler.recordTimer("crew.spike.apply.business.test", "seq", "on", "success", 10_000_000L);
		spikeApplyProfiler.recordSummary("crew.spike.apply.queue.length.snapshot", "seq", "on", "success", 12.0);
		MDC.put("traceId", "trace-1");
		MDC.put("requestInfo", "POST /meeting/v2/apply/undefined");
		MDC.put("userId", "744");
		spikeApplyProfiler.recordTimer("crew.spike.apply.envelope.request.total", "fat", "on", "success",
			20_000_000L);
		MDC.clear();

		// when
		SpikeProfilerResetResponseDto response = spikeApplyProfiler.reset();

		// then
		assertThat(response.clearedTimers()).isEqualTo(2);
		assertThat(response.clearedSummaries()).isEqualTo(1);
		assertThat(response.clearedTraces()).isEqualTo(1);
		assertThat(spikeApplyProfiler.snapshot().timers()).isEmpty();
		assertThat(spikeApplyProfiler.snapshot().summaries()).isEmpty();
		assertThat(spikeApplyProfiler.traceSnapshot().traces()).isEmpty();
	}

	@Test
	void traceSnapshot_요청별_traceId_기준_metric을_반환한다() {
		// given
		MDC.put("traceId", "trace-123");
		MDC.put("requestInfo", "POST /meeting/v2/apply/undefined");
		MDC.put("userId", "744");
		spikeApplyProfiler.recordTimer("crew.spike.apply.envelope.app_edge.total", "fat", "on", "success",
			30_000_000L);
		spikeApplyProfiler.recordTimer("crew.spike.apply.envelope.jwt.get_public_key.total", "fat", "on", "success",
			10_000_000L);
		spikeApplyProfiler.recordTimer("crew.spike.apply.envelope.jwt.get_public_key.total", "fat", "on", "success",
			20_000_000L);
		spikeApplyProfiler.recordSummary("crew.spike.apply.envelope.jwk.cache.hit", "fat", "on", "observed", 1.0);
		MDC.clear();

		// when
		SpikeProfilerTraceSnapshotResponseDto traceSnapshot = spikeApplyProfiler.traceSnapshot();

		// then
		assertThat(traceSnapshot.traces()).hasSize(1);
		SpikeProfilerTraceDto trace = traceSnapshot.traces().get(0);
		assertThat(trace.traceId()).isEqualTo("trace-123");
		assertThat(trace.requestInfo()).isEqualTo("POST /meeting/v2/apply/undefined");
		assertThat(trace.userId()).isEqualTo("744");
		assertThat(trace.completedAt()).isNotNull();
		assertThat(trace.finalOutcome()).isEqualTo("success");
		assertThat(trace.metrics()).extracting(metric -> metric.metricName())
			.containsExactlyInAnyOrder(
				"crew.spike.apply.envelope.app_edge.total",
				"crew.spike.apply.envelope.jwt.get_public_key.total",
				"crew.spike.apply.envelope.jwt.get_public_key.total",
				"crew.spike.apply.envelope.jwk.cache.hit"
			);
	}

	@Test
	void diagnosticsDisabled_keepsCoarseMetrics_onlyAndDisablesTraceStore() {
		SpikeDiagnosticProperties properties = new SpikeDiagnosticProperties();
		properties.setEnabled(false);
		SpikeApplyProfiler disabledProfiler = new SpikeApplyProfiler(properties);

		disabledProfiler.recordTimer(SpikeApplyMetrics.METRIC_REQUEST_TOTAL, "fat", "on", "success", 10_000_000L);
		disabledProfiler.recordTimer(SpikeApplyMetrics.METRIC_JWT_PARSE_TOTAL, "fat", "on", "success", 10_000_000L);
		disabledProfiler.recordSummary("crew.spike.apply.queue.length.snapshot", "fat", "on", "success", 1.0);
		disabledProfiler.recordSummary(SpikeApplyMetrics.METRIC_JWK_CACHE_HIT, "fat", "on", "observed", 1.0);

		SpikeProfilerSnapshotResponseDto snapshot = disabledProfiler.snapshot();

		assertThat(snapshot.timers()).extracting(SpikeProfilerTimerMetricDto::metricName)
			.containsExactly(SpikeApplyMetrics.METRIC_REQUEST_TOTAL);
		assertThat(snapshot.summaries()).extracting(summary -> summary.metricName())
			.containsExactly("crew.spike.apply.queue.length.snapshot");
		assertThat(disabledProfiler.traceSnapshot().traces()).isEmpty();
	}
}
