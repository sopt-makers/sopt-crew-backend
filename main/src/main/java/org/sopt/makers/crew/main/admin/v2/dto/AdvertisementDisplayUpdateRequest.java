package org.sopt.makers.crew.main.admin.v2.dto;

import jakarta.validation.constraints.NotNull;

public record AdvertisementDisplayUpdateRequest(
	@NotNull
	Boolean isDisplay
) {
}
