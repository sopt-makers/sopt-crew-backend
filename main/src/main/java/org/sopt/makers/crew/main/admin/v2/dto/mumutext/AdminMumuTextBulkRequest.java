package org.sopt.makers.crew.main.admin.v2.dto.mumutext;

import jakarta.validation.constraints.NotBlank;

public record AdminMumuTextBulkRequest(
	@NotBlank(message = "CSV 내용은 필수입니다.")
	String csvText
) {
}
