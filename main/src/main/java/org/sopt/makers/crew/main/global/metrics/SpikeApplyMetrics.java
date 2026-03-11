package org.sopt.makers.crew.main.global.metrics;

public final class SpikeApplyMetrics {
	public static final String REQUEST_MATCHED_ATTRIBUTE = SpikeApplyMetrics.class.getName() + ".matched";
	public static final String MDC_ACTIVE_KEY = SpikeApplyMetrics.class.getName() + ".active";
	public static final String MDC_ACTIVE_PREVIOUS = SpikeApplyMetrics.class.getName() + ".previous";

	public static final String OUTCOME_SUCCESS = "success";
	public static final String OUTCOME_ERROR = "error";
	public static final String OUTCOME_OBSERVED = "observed";

	public static final String METRIC_REQUEST_TOTAL = "crew.spike.apply.envelope.request.total";
	public static final String METRIC_AUTH_TOTAL = "crew.spike.apply.envelope.auth.total";
	public static final String METRIC_JWT_VERIFY_TOTAL = "crew.spike.apply.envelope.jwt.verify.total";
	public static final String METRIC_CONTROLLER_ENTRY = "crew.spike.apply.envelope.controller.entry";
	public static final String METRIC_JWK_CACHE_HIT = "crew.spike.apply.envelope.jwk.cache.hit";
	public static final String METRIC_JWK_CACHE_MISS = "crew.spike.apply.envelope.jwk.cache.miss";

	private SpikeApplyMetrics() {
	}
}
