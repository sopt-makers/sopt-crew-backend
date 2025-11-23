package org.sopt.makers.crew.main.internal;

import org.sopt.makers.crew.main.global.pagination.dto.PageOptionsDto;
import org.sopt.makers.crew.main.internal.dto.InternalMeetingGetAllMeetingDto;
import org.sopt.makers.crew.main.internal.dto.InternalMeetingGetAllWritingPostResponseDto;
import org.sopt.makers.crew.main.internal.dto.InternalUserAppliedMeetingResponseDto;
import org.sopt.makers.crew.main.meeting.v2.dto.query.MeetingV2GetAllMeetingQueryDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "[Internal] 모임")
public interface InternalMeetingApi {

	@Operation(summary = "[Internal] 모임 전체 조회/검색/필터링", description = "모임 전체 조회/검색/필터링")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "모임 목록 조회 성공")})
	@Parameters(value = {
		@Parameter(name = "page", description = "페이지", example = "1", required = true, schema = @Schema(type = "integer", format = "int32")),
		@Parameter(name = "take", description = "가져올 데이터 개수", example = "10", required = true, schema = @Schema(type = "integer", format = "int32")),
		@Parameter(name = "category", description = "카테고리", example = "[스터디, 행사, 세미나]", required = true, array = @ArraySchema(schema = @Schema(type = "string"))),
		@Parameter(name = "isOnlyActiveGeneration", description = "활동기수만 참여여부", example = "true", required = true, schema = @Schema(type = "boolean", format = "boolean")),
	})
	ResponseEntity<InternalMeetingGetAllMeetingDto> getMeetings(
		@ModelAttribute @Valid @Parameter(hidden = true) MeetingV2GetAllMeetingQueryDto queryCommand,
		@RequestParam @Valid Integer orgId);

	@Operation(summary = "[Internal] 모임 전체 조회", description = "플그 피드 작성시 크루 모임 전체 조회를 위한 api")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "모임 목록 조회 성공")})
	@Parameters(value = {
		@Parameter(name = "page", description = "페이지", example = "1", required = true, schema = @Schema(type = "integer", format = "int32")),
		@Parameter(name = "take", description = "가져올 데이터 개수", example = "10", required = true, schema = @Schema(type = "integer", format = "int32")),
	})
	ResponseEntity<InternalMeetingGetAllWritingPostResponseDto> getMeetingsForWritingPost(
		@ModelAttribute @Valid @Parameter(hidden = true) PageOptionsDto pageOptionsDto
	);

	@Operation(summary = "[Internal] 모임 정보 조회", description = "플그 요청에 따른 맴버에 따라 크루 모임 조회를 위한 api")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "모임 목록 조회 성공")})
	@Parameters(value = {
		@Parameter(name = "userId", description = "찾고자 하는 userId", example = "10", required = true, schema = @Schema(type = "integer", format = "int32")),
	})
	ResponseEntity<InternalUserAppliedMeetingResponseDto> getAppliedMeetingInfo(
		@PathVariable @Valid Integer userId
	);

}
