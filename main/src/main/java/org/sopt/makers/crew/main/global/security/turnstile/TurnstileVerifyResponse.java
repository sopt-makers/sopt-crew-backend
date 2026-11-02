package org.sopt.makers.crew.main.global.security.turnstile;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TurnstileVerifyResponse(
	boolean success,
	String hostname,
	String action,
	@JsonProperty("challenge_ts") String challengeTs,
	@JsonProperty("error-codes") List<String> errorCodes
) {
}
