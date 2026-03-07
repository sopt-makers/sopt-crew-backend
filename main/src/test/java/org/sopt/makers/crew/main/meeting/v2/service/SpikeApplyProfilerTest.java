package org.sopt.makers.crew.main.meeting.v2.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.sopt.makers.crew.main.internal.dto.SpikeProfilerResetResponseDto;
import org.sopt.makers.crew.main.internal.dto.SpikeProfilerSnapshotResponseDto;
import org.sopt.makers.crew.main.internal.dto.SpikeProfilerTimerMetricDto;

class SpikeApplyProfilerTest {

	private final SpikeApplyProfiler spikeApplyProfiler = new SpikeApplyProfiler();

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

		// when
		SpikeProfilerResetResponseDto response = spikeApplyProfiler.reset();

		// then
		assertThat(response.clearedTimers()).isEqualTo(1);
		assertThat(response.clearedSummaries()).isEqualTo(1);
		assertThat(spikeApplyProfiler.snapshot().timers()).isEmpty();
		assertThat(spikeApplyProfiler.snapshot().summaries()).isEmpty();
	}
}
