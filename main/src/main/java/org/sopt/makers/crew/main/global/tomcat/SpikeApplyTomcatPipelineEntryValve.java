package org.sopt.makers.crew.main.global.tomcat;

import static org.sopt.makers.crew.main.global.metrics.SpikeApplyMetrics.METRIC_TOMCAT_PIPELINE_ENTRY_TOTAL;
import static org.sopt.makers.crew.main.global.metrics.SpikeApplyMetrics.OUTCOME_ERROR;
import static org.sopt.makers.crew.main.global.metrics.SpikeApplyMetrics.OUTCOME_SUCCESS;
import static org.sopt.makers.crew.main.global.metrics.SpikeApplyMetrics.REQUEST_INFO_ATTRIBUTE;
import static org.sopt.makers.crew.main.global.metrics.SpikeApplyMetrics.TOMCAT_PIPELINE_ENTRY_RECORDED_ATTRIBUTE;
import static org.sopt.makers.crew.main.global.metrics.SpikeApplyMetrics.TOMCAT_PIPELINE_ENTRY_START_NANOS_ATTRIBUTE;
import static org.sopt.makers.crew.main.global.metrics.SpikeApplyMetrics.TRACE_ID_ATTRIBUTE;

import java.io.IOException;
import java.util.UUID;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.slf4j.MDC;
import org.sopt.makers.crew.main.global.metrics.SpikeApplyMetricRecorder;
import org.sopt.makers.crew.main.global.metrics.SpikeApplyRequestSupport;
import org.sopt.makers.crew.main.global.metrics.SpikeApplyRuntimeConfig;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;

@Component
public class SpikeApplyTomcatPipelineEntryValve extends ValveBase {
	private static final String TRACE_ID_MDC_KEY = "traceId";
	private static final String REQUEST_INFO_MDC_KEY = "requestInfo";
	private static final String X_CORRELATION_ID = "X-Correlation-ID";
	private static final String X_REQUEST_ID = "X-Request-ID";

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
					String previousTraceId = MDC.get(TRACE_ID_MDC_KEY);
					String previousRequestInfo = MDC.get(REQUEST_INFO_MDC_KEY);
					String traceId = resolveTraceId(request);
					String requestInfo = request.getMethod() + " " + request.getRequestURI();
					MDC.put(TRACE_ID_MDC_KEY, traceId);
					MDC.put(REQUEST_INFO_MDC_KEY, requestInfo);
					request.setAttribute(TRACE_ID_ATTRIBUTE, traceId);
					request.setAttribute(REQUEST_INFO_ATTRIBUTE, requestInfo);
					try {
						spikeApplyMetricRecorder.recordTimer(
							METRIC_TOMCAT_PIPELINE_ENTRY_TOTAL,
							SpikeApplyRuntimeConfig.currentTxMode(),
							SpikeApplyRuntimeConfig.currentGate(),
							outcome,
							Math.max(0L, System.nanoTime() - startNanos)
						);
					} finally {
						restoreMdc(TRACE_ID_MDC_KEY, previousTraceId);
						restoreMdc(REQUEST_INFO_MDC_KEY, previousRequestInfo);
					}
					request.setAttribute(TOMCAT_PIPELINE_ENTRY_RECORDED_ATTRIBUTE, Boolean.TRUE);
				}
			}
		}
	}

	private String resolveTraceId(Request request) {
		// Keep this logic aligned with MdcLoggingFilter.resolveTraceId(); the Valve runs earlier,
		// but the same request must resolve to the same canonical trace ID in both places.
		Object existingTraceId = request.getAttribute(TRACE_ID_ATTRIBUTE);
		if (existingTraceId instanceof String traceId && !traceId.isBlank()) {
			return traceId;
		}
		String correlationId = request.getHeader(X_CORRELATION_ID);
		if (correlationId != null && !correlationId.isBlank()) {
			return correlationId;
		}
		String requestId = request.getHeader(X_REQUEST_ID);
		if (requestId != null && !requestId.isBlank()) {
			return requestId;
		}
		return UUID.randomUUID().toString();
	}

	private void restoreMdc(String key, String previousValue) {
		if (previousValue == null || previousValue.isBlank()) {
			MDC.remove(key);
			return;
		}
		MDC.put(key, previousValue);
	}
}
