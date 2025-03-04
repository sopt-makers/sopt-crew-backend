package org.sopt.makers.crew.main.internal;

import org.sopt.makers.crew.main.internal.dto.ApprovedStudyCountResponseDto;
import org.sopt.makers.crew.main.internal.dto.InternalMeetingCountResponseDto;
import org.sopt.makers.crew.main.internal.dto.TopFastestAppliedMeetingsResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "[Internal] 모임 통계(수치)")
public interface InternalMeetingStatsApi {

	@Operation(summary = "[Internal] 모임 유저의 승인된 스터디 수 조회", description = "특정 유저의 승인된 스터디 수를 조회하는 API입니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "APPROVE된 스터디 수 조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"orgId\": 1, \"approvedStudyCount\": 5}"))),
		@ApiResponse(responseCode = "404", description = "존재하지 않는 유저입니다.", content = @Content(mediaType = "application/json"))})
	ResponseEntity<ApprovedStudyCountResponseDto> getApprovedStudyCountByOrgId(
		@Parameter(description = "플레이그라운드 유저 ID(orgId)", example = "1") Integer orgId);

	@Operation(summary = "[Internal] 특정 유저가 가장 빠르게 신청한 모임 N개 조회", description = "특정 유저가 가장 빠르게 신청한 모임 N개 조회하는 API입니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "가장 빠르게 신청한 모임 조회 성공"),
		@ApiResponse(responseCode = "404", description = "존재하지 않는 유저입니다.", content = @Content(mediaType = "application/json"))})
	ResponseEntity<TopFastestAppliedMeetingsResponseDto> getTopFastestAppliedMeetings(
		@Parameter(description = "플레이그라운드 유저 ID(orgId)", example = "1") Integer orgId,
		@RequestParam(name = "query-count", required = false, defaultValue = "3") Integer queryCount,
		@RequestParam(name = "query-year") Integer queryYear
		);

	@Operation(summary = "[Internal] 특정 기수의 총 스터디 개수 반환", description = "특정 기수의 총 스터디 개수를 반환합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "특정 기수의 총 스터디 개수 조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"meetingCount\": 1}"))),
		@ApiResponse(responseCode = "400", description = "올바르지 않은 요청입니다.", content = @Content(mediaType = "application/json"))})
	ResponseEntity<InternalMeetingCountResponseDto> getMeetingCountByGeneration(
		@RequestParam(name = "generation") Integer generation);
}
