package org.sopt.makers.crew.main.meeting.v2.service;

import java.time.Instant;
import java.util.Arrays;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.sopt.makers.crew.main.global.metrics.SpikeApplyMetricRecorder;
import org.sopt.makers.crew.main.internal.dto.SpikeProfilerResetResponseDto;
import org.sopt.makers.crew.main.internal.dto.SpikeProfilerSnapshotResponseDto;
import org.sopt.makers.crew.main.internal.dto.SpikeProfilerSummaryMetricDto;
import org.sopt.makers.crew.main.internal.dto.SpikeProfilerTimerMetricDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class SpikeApplyProfiler implements SpikeApplyMetricRecorder {

	public static final String OUTCOME_SUCCESS = "success";
	public static final String OUTCOME_ERROR = "error";
	private static final int MAX_SAMPLES_PER_METRIC = 1024;

	private final ConcurrentMap<MetricKey, TimerStats> timerStats = new ConcurrentHashMap<>();
	private final ConcurrentMap<MetricKey, SummaryStats> summaryStats = new ConcurrentHashMap<>();
	@Autowired(required = false)
	private Environment environment;
	private volatile boolean enabled = true;

	@PostConstruct
	void configureEnabledProfiles() {
		if (environment == null) {
			return;
		}
		enabled = Arrays.stream(environment.getActiveProfiles())
			.anyMatch(profile -> "local".equals(profile) || "traffic".equals(profile));
	}

	public void recordTimer(String metricName, String txMode, String gate, String outcome, long nanos) {
		if (!enabled) {
			return;
		}
		if (nanos <= 0) {
			return;
		}
		timerStats.computeIfAbsent(new MetricKey(metricName, txMode, gate, outcome), ignored -> new TimerStats())
			.record(nanos);
	}

	public void recordSummary(String metricName, String txMode, String gate, String outcome, double value) {
		if (!enabled) {
			return;
		}
		summaryStats.computeIfAbsent(new MetricKey(metricName, txMode, gate, outcome), ignored -> new SummaryStats())
			.record(value);
	}

	public <T> T profile(String metricName, String txMode, String gate, StepSupplier<T> supplier) {
		long start = System.nanoTime();
		try {
			T result = supplier.get();
			recordTimer(metricName, txMode, gate, OUTCOME_SUCCESS, System.nanoTime() - start);
			return result;
		} catch (RuntimeException exception) {
			recordTimer(metricName, txMode, gate, OUTCOME_ERROR, System.nanoTime() - start);
			throw exception;
		}
	}

	public void profile(String metricName, String txMode, String gate, StepRunnable runnable) {
		long start = System.nanoTime();
		try {
			runnable.run();
			recordTimer(metricName, txMode, gate, OUTCOME_SUCCESS, System.nanoTime() - start);
		} catch (RuntimeException exception) {
			recordTimer(metricName, txMode, gate, OUTCOME_ERROR, System.nanoTime() - start);
			throw exception;
		}
	}

	public SpikeProfilerSnapshotResponseDto snapshot() {
		if (!enabled) {
			return new SpikeProfilerSnapshotResponseDto(Instant.now().toString(), List.of(), List.of());
		}
		List<SpikeProfilerTimerMetricDto> timers = timerStats.entrySet().stream()
			.map(entry -> entry.getValue().toDto(entry.getKey()))
			.sorted(Comparator.comparing(SpikeProfilerTimerMetricDto::metricName)
				.thenComparing(SpikeProfilerTimerMetricDto::txMode)
				.thenComparing(SpikeProfilerTimerMetricDto::gate)
				.thenComparing(SpikeProfilerTimerMetricDto::outcome))
			.toList();

		List<SpikeProfilerSummaryMetricDto> summaries = summaryStats.entrySet().stream()
			.map(entry -> entry.getValue().toDto(entry.getKey()))
			.sorted(Comparator.comparing(SpikeProfilerSummaryMetricDto::metricName)
				.thenComparing(SpikeProfilerSummaryMetricDto::txMode)
				.thenComparing(SpikeProfilerSummaryMetricDto::gate)
				.thenComparing(SpikeProfilerSummaryMetricDto::outcome))
			.toList();

		return new SpikeProfilerSnapshotResponseDto(Instant.now().toString(), timers, summaries);
	}

	public SpikeProfilerResetResponseDto reset() {
		int clearedTimers = timerStats.size();
		int clearedSummaries = summaryStats.size();
		timerStats.clear();
		summaryStats.clear();
		return new SpikeProfilerResetResponseDto(Instant.now().toString(), clearedTimers, clearedSummaries);
	}

	@FunctionalInterface
	public interface StepSupplier<T> {
		T get();
	}

	@FunctionalInterface
	public interface StepRunnable {
		void run();
	}

	private record MetricKey(String metricName, String txMode, String gate, String outcome) {
	}

	private static final class TimerStats {
		private final ArrayDeque<Long> samples = new ArrayDeque<>(MAX_SAMPLES_PER_METRIC);

		private void record(long nanos) {
			synchronized (samples) {
				if (samples.size() == MAX_SAMPLES_PER_METRIC) {
					samples.removeFirst();
				}
				samples.addLast(nanos);
			}
		}

		private SpikeProfilerTimerMetricDto toDto(MetricKey key) {
			long[] sorted;
			synchronized (samples) {
				sorted = samples.stream().mapToLong(Long::longValue).sorted().toArray();
			}
			long currentCount = sorted.length;
			double totalMs = nanosToMillis(Arrays.stream(sorted).sum());
			double meanMs = currentCount == 0 ? 0.0 : totalMs / currentCount;
			double minMs = currentCount == 0 ? 0.0 : nanosToMillis(sorted[0]);
			double maxMs = currentCount == 0 ? 0.0 : nanosToMillis(sorted[sorted.length - 1]);

			return new SpikeProfilerTimerMetricDto(
				key.metricName(),
				key.txMode(),
				key.gate(),
				key.outcome(),
				currentCount,
				totalMs,
				meanMs,
				minMs,
				maxMs,
				nanosToMillis(percentile(sorted, 0.50)),
				nanosToMillis(percentile(sorted, 0.90)),
				nanosToMillis(percentile(sorted, 0.95)),
				nanosToMillis(percentile(sorted, 0.99))
			);
		}
	}

	private static final class SummaryStats {
		private final ArrayDeque<Double> samples = new ArrayDeque<>(MAX_SAMPLES_PER_METRIC);

		private void record(double value) {
			synchronized (samples) {
				if (samples.size() == MAX_SAMPLES_PER_METRIC) {
					samples.removeFirst();
				}
				samples.addLast(value);
			}
		}

		private SpikeProfilerSummaryMetricDto toDto(MetricKey key) {
			double[] sorted;
			synchronized (samples) {
				sorted = samples.stream().mapToDouble(Double::doubleValue).sorted().toArray();
			}
			long currentCount = sorted.length;
			double mean = currentCount == 0 ? 0.0 : Arrays.stream(sorted).sum() / currentCount;
			double min = currentCount == 0 ? 0.0 : sorted[0];
			double max = currentCount == 0 ? 0.0 : sorted[sorted.length - 1];

			return new SpikeProfilerSummaryMetricDto(
				key.metricName(),
				key.txMode(),
				key.gate(),
				key.outcome(),
				currentCount,
				mean,
				min,
				max,
				percentile(sorted, 0.50),
				percentile(sorted, 0.90),
				percentile(sorted, 0.95),
				percentile(sorted, 0.99)
			);
		}
	}

	private static long percentile(long[] sorted, double quantile) {
		if (sorted.length == 0) {
			return 0L;
		}
		int index = Math.max(0, (int)Math.ceil(quantile * sorted.length) - 1);
		return sorted[Math.min(index, sorted.length - 1)];
	}

	private static double percentile(double[] sorted, double quantile) {
		if (sorted.length == 0) {
			return 0.0;
		}
		int index = Math.max(0, (int)Math.ceil(quantile * sorted.length) - 1);
		return sorted[Math.min(index, sorted.length - 1)];
	}

	private static double nanosToMillis(long nanos) {
		return nanos / 1_000_000.0;
	}
}
