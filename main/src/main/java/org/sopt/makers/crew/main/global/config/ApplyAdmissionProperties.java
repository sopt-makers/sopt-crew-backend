package org.sopt.makers.crew.main.global.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Validated
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "meeting.apply.admission")
public class ApplyAdmissionProperties {

	private final boolean enabled;

	@Min(1)
	private final int permits;

	@NotNull
	private final Duration waitTimeout;

	@AssertTrue(message = "meeting.apply.admission.wait-timeout은 0 이상이어야 합니다.")
	public boolean isWaitTimeoutNonNegative() {
		return waitTimeout != null && !waitTimeout.isNegative();
	}
}
