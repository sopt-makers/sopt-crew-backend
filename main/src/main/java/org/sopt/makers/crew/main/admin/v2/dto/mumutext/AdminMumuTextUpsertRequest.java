package org.sopt.makers.crew.main.admin.v2.dto.mumutext;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminMumuTextUpsertRequest(
	@NotBlank(message = "질문 문구는 필수입니다.")
	String text,

	@NotBlank(message = "구분은 필수입니다.")
	String category,

	@NotNull(message = "노출 시작일시는 필수입니다.")
	LocalDateTime showStartDate,

	@NotNull(message = "노출 종료일시는 필수입니다.")
	LocalDateTime showEndDate
) {
	public String normalizedText() {
		return text == null ? null : text.trim();
	}

	public String normalizedCategory() {
		return category == null ? null : category.trim();
	}
}
