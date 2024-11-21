package org.sopt.makers.crew.main.internal;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "internal API - 개수(스터디, 행사 등등)")
public interface InternalMeetingStatsApi {

	@Operation(summary = "유저의 승인된 스터디 수 조회", description = "특정 유저의 승인된 스터디 수를 조회하는 API입니다.", tags = {
		"Internal Meeting Stats"})
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "APPROVE된 스터디 수 조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"orgId\": 1, \"approvedStudyCount\": 5}"))),
		@ApiResponse(responseCode = "404", description = "존재하지 않는 유저입니다.", content = @Content(mediaType = "application/json"))})
	ResponseEntity<Map<String, Object>> getApprovedStudyCountByOrgId(
		@Parameter(description = "플레이그라운드 유저 ID(orgId)", example = "1") Integer orgId);
}
