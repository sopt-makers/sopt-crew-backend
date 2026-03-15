package org.sopt.makers.crew.main.global.metrics;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spike.diagnostic")
public class SpikeDiagnosticProperties {
	private boolean enabled = true;
	private final Selective selective = new Selective();

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Selective getSelective() {
		return selective;
	}

	public boolean isSelectiveEnabled() {
		return enabled && selective.enabled
			&& (!selective.timerAllowlist.isEmpty() || !selective.summaryAllowlist.isEmpty());
	}

	public boolean isDetailedTimerEnabled(String metricName) {
		return enabled && (!isSelectiveEnabled() || selective.timerAllowlist.contains(metricName));
	}

	public boolean isDetailedSummaryEnabled(String metricName) {
		return enabled && (!isSelectiveEnabled() || selective.summaryAllowlist.contains(metricName));
	}

	public static class Selective {
		private boolean enabled;
		private List<String> timerAllowlist = new ArrayList<>();
		private List<String> summaryAllowlist = new ArrayList<>();

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public List<String> getTimerAllowlist() {
			return timerAllowlist;
		}

		public void setTimerAllowlist(List<String> timerAllowlist) {
			this.timerAllowlist = timerAllowlist != null ? new ArrayList<>(timerAllowlist) : new ArrayList<>();
		}

		public List<String> getSummaryAllowlist() {
			return summaryAllowlist;
		}

		public void setSummaryAllowlist(List<String> summaryAllowlist) {
			this.summaryAllowlist = summaryAllowlist != null ? new ArrayList<>(summaryAllowlist) : new ArrayList<>();
		}
	}
}
