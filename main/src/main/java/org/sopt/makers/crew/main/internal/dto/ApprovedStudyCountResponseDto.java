package org.sopt.makers.crew.main.internal.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "승인된 스터디 수를 나타내는 DTO")
public record ApprovedStudyCountResponseDto(
	@Schema(description = "플레이그라운드 유저 ID(orgId)", example = "1")
	Integer orgId,

	@Schema(description = "승인된 스터디 수", example = "5")
	Long approvedStudyCount
) {
	public static ApprovedStudyCountResponseDto of(Integer orgId, Long approvedStudyCount) {
		return new ApprovedStudyCountResponseDto(orgId, approvedStudyCount);
	}
}
