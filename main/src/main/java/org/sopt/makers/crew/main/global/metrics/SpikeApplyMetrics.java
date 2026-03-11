package org.sopt.makers.crew.main.global.metrics;

public final class SpikeApplyMetrics {
	public static final String REQUEST_MATCHED_ATTRIBUTE = SpikeApplyMetrics.class.getName() + ".matched";
	public static final String REQUEST_TOTAL_NANOS_ATTRIBUTE = SpikeApplyMetrics.class.getName() + ".requestTotalNanos";
	public static final String TRACE_ID_ATTRIBUTE = SpikeApplyMetrics.class.getName() + ".traceId";
	public static final String USER_ID_ATTRIBUTE = SpikeApplyMetrics.class.getName() + ".userId";
	public static final String CLIENT_IP_ATTRIBUTE = SpikeApplyMetrics.class.getName() + ".clientIp";
	public static final String REQUEST_INFO_ATTRIBUTE = SpikeApplyMetrics.class.getName() + ".requestInfo";
	public static final String MDC_ACTIVE_KEY = SpikeApplyMetrics.class.getName() + ".active";
	public static final String MDC_ACTIVE_PREVIOUS = SpikeApplyMetrics.class.getName() + ".previous";

	public static final String OUTCOME_SUCCESS = "success";
	public static final String OUTCOME_ERROR = "error";
	public static final String OUTCOME_OBSERVED = "observed";

	public static final String METRIC_APP_EDGE_TOTAL = "crew.spike.apply.envelope.app_edge.total";
	public static final String METRIC_APP_EDGE_PRE_REQUEST_TOTAL = "crew.spike.apply.envelope.app_edge.pre_request.total";
	public static final String METRIC_REQUEST_TOTAL = "crew.spike.apply.envelope.request.total";
	public static final String METRIC_AUTH_TOTAL = "crew.spike.apply.envelope.auth.total";
	public static final String METRIC_JWT_VERIFY_TOTAL = "crew.spike.apply.envelope.jwt.verify.total";
	public static final String METRIC_JWT_VERIFY_UNATTRIBUTED_TOTAL = "crew.spike.apply.envelope.jwt.verify.unattributed.total";
	public static final String METRIC_JWT_VERIFY_WITH_RETRY_TOTAL = "crew.spike.apply.envelope.jwt.verify_with_retry.total";
	public static final String METRIC_JWT_TO_AUTHENTICATION_TOTAL = "crew.spike.apply.envelope.jwt.to_authentication.total";
	public static final String METRIC_JWT_PARSE_TOTAL = "crew.spike.apply.envelope.jwt.parse.total";
	public static final String METRIC_JWT_EXTRACT_KID_TOTAL = "crew.spike.apply.envelope.jwt.extract_kid.total";
	public static final String METRIC_JWT_GET_PUBLIC_KEY_TOTAL = "crew.spike.apply.envelope.jwt.get_public_key.total";
	public static final String METRIC_JWT_GET_PUBLIC_KEY_CACHE_GET_IF_PRESENT_TOTAL =
		"crew.spike.apply.envelope.jwt.get_public_key.cache_get_if_present.total";
	public static final String METRIC_JWT_GET_PUBLIC_KEY_HIT_BOOKKEEPING_TOTAL =
		"crew.spike.apply.envelope.jwt.get_public_key.hit_bookkeeping.total";
	public static final String METRIC_JWT_GET_PUBLIC_KEY_MISS_LOAD_TOTAL =
		"crew.spike.apply.envelope.jwt.get_public_key.miss_load.total";
	public static final String METRIC_JWT_SIGNATURE_VERIFY_TOTAL = "crew.spike.apply.envelope.jwt.signature_verify.total";
	public static final String METRIC_JWT_CLAIMS_VALIDATE_TOTAL = "crew.spike.apply.envelope.jwt.claims_validate.total";
	public static final String METRIC_JWT_RETRY_USED = "crew.spike.apply.envelope.jwt.retry.used";
	public static final String METRIC_JWT_RETRY_NOT_USED = "crew.spike.apply.envelope.jwt.retry.not_used";
	public static final String METRIC_CONTROLLER_ENTRY = "crew.spike.apply.envelope.controller.entry";
	public static final String METRIC_JWK_CACHE_HIT = "crew.spike.apply.envelope.jwk.cache.hit";
	public static final String METRIC_JWK_CACHE_MISS = "crew.spike.apply.envelope.jwk.cache.miss";

	private SpikeApplyMetrics() {
	}
}
