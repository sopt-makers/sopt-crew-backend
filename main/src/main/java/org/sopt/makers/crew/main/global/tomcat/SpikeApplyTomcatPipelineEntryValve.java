package org.sopt.makers.crew.main.global.tomcat;

import static org.sopt.makers.crew.main.global.metrics.SpikeApplyMetrics.METRIC_TOMCAT_PIPELINE_ENTRY_TOTAL;
import static org.sopt.makers.crew.main.global.metrics.SpikeApplyMetrics.OUTCOME_ERROR;
import static org.sopt.makers.crew.main.global.metrics.SpikeApplyMetrics.OUTCOME_SUCCESS;
import static org.sopt.makers.crew.main.global.metrics.SpikeApplyMetrics.TOMCAT_PIPELINE_ENTRY_RECORDED_ATTRIBUTE;
import static org.sopt.makers.crew.main.global.metrics.SpikeApplyMetrics.TOMCAT_PIPELINE_ENTRY_START_NANOS_ATTRIBUTE;

import java.io.IOException;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.sopt.makers.crew.main.global.metrics.SpikeApplyMetricRecorder;
import org.sopt.makers.crew.main.global.metrics.SpikeApplyRequestSupport;
import org.sopt.makers.crew.main.global.metrics.SpikeApplyRuntimeConfig;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;

@Component
public class SpikeApplyTomcatPipelineEntryValve extends ValveBase {
	private final SpikeApplyRequestSupport spikeApplyRequestSupport;
	private final SpikeApplyMetricRecorder spikeApplyMetricRecorder;

	public SpikeApplyTomcatPipelineEntryValve(
		SpikeApplyRequestSupport spikeApplyRequestSupport,
		SpikeApplyMetricRecorder spikeApplyMetricRecorder
	) {
		super(true);
		this.spikeApplyRequestSupport = spikeApplyRequestSupport;
		this.spikeApplyMetricRecorder = spikeApplyMetricRecorder;
	}

	@Override
	public void invoke(Request request, Response response) throws IOException, ServletException {
		boolean isSpikeApplyRequest = spikeApplyRequestSupport.isSpikeApplyRequest(request);
		boolean firstPipelineEntry = isSpikeApplyRequest
			&& request.getAttribute(TOMCAT_PIPELINE_ENTRY_START_NANOS_ATTRIBUTE) == null;

		if (firstPipelineEntry) {
			request.setAttribute(TOMCAT_PIPELINE_ENTRY_START_NANOS_ATTRIBUTE, System.nanoTime());
		}

		String outcome = OUTCOME_SUCCESS;
		try {
			getNext().invoke(request, response);
			if (isSpikeApplyRequest && response.getStatus() >= 400) {
				outcome = OUTCOME_ERROR;
			}
		} catch (IOException | ServletException | RuntimeException exception) {
			outcome = OUTCOME_ERROR;
			throw exception;
		} finally {
			if (firstPipelineEntry && request.getAttribute(TOMCAT_PIPELINE_ENTRY_RECORDED_ATTRIBUTE) == null) {
				Object startAttr = request.getAttribute(TOMCAT_PIPELINE_ENTRY_START_NANOS_ATTRIBUTE);
				if (startAttr instanceof Long startNanos) {
					spikeApplyMetricRecorder.recordTimer(
						METRIC_TOMCAT_PIPELINE_ENTRY_TOTAL,
						SpikeApplyRuntimeConfig.currentTxMode(),
						SpikeApplyRuntimeConfig.currentGate(),
						outcome,
						Math.max(0L, System.nanoTime() - startNanos)
					);
					request.setAttribute(TOMCAT_PIPELINE_ENTRY_RECORDED_ATTRIBUTE, Boolean.TRUE);
				}
			}
		}
	}
}
