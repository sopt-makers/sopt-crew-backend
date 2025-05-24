package org.sopt.makers.crew.main.user.v2.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record UpdateUserInterestKeywordRequestDto(
	@NotNull @NotEmpty List<String> keywords) {
}
