package org.sopt.makers.crew.main.meeting.v2;

import java.security.Principal;
import java.util.List;

import org.sopt.makers.crew.main.meeting.v2.dto.query.MeetingGetAppliesCsvQueryDto;
import org.sopt.makers.crew.main.meeting.v2.dto.query.MeetingGetAppliesQueryDto;
import org.sopt.makers.crew.main.meeting.v2.dto.query.MeetingV2GetAllMeetingByOrgUserQueryDto;
import org.sopt.makers.crew.main.meeting.v2.dto.query.MeetingV2GetAllMeetingQueryDto;
import org.sopt.makers.crew.main.meeting.v2.dto.request.ApplyV2UpdateStatusBodyDto;
import org.sopt.makers.crew.main.meeting.v2.dto.request.MeetingV2ApplyMeetingDto;
import org.sopt.makers.crew.main.meeting.v2.dto.request.MeetingV2CreateAndUpdateMeetingBodyDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.AppliesCsvFileUrlResponseDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingGetApplyListResponseDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2ApplyMeetingResponseDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2CreateMeetingResponseDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2GetAllMeetingByOrgUserDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2GetAllMeetingDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2GetMeetingBannerResponseDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2GetMeetingByIdResponseDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2GetRecommendDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.PreSignedUrlResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.websocket.server.PathParam;

@Tag(name = "모임")
public interface MeetingV2Api {
	@Operation(summary = "플레이그라운드 마이페이지 내 모임 정보 조회")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "성공")})
	@Parameters({@Parameter(name = "page", description = "페이지, default = 1", example = "1"),
		@Parameter(name = "take", description = "가져올 데이터 개수, default = 12", example = "50"),
		@Parameter(name = "orgUserId", description = "플레이그라운드 유저 id", example = "0")})
	ResponseEntity<MeetingV2GetAllMeetingByOrgUserDto> getAllMeetingByOrgUser(
		@ModelAttribute @Parameter(hidden = true) MeetingV2GetAllMeetingByOrgUserQueryDto queryDto);

	@Operation(summary = "모임 둘러보기 조회")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "성공"),
		@ApiResponse(responseCode = "204", description = "모임이 없습니다.", content = @Content),})
	ResponseEntity<List<MeetingV2GetMeetingBannerResponseDto>> getMeetingBanner(
		Principal principal);

	@Operation(summary = "모임 생성")
	@ApiResponses(value = {@ApiResponse(responseCode = "201", description = "성공"),
		@ApiResponse(responseCode = "400", description = "\"이미지 파일이 없습니다.\" or \"한 개 이상의 파트를 입력해주세요\" or \"프로필을 입력해주세요\"", content = @Content),})
	ResponseEntity<MeetingV2CreateMeetingResponseDto> createMeeting(
		@Valid @RequestBody MeetingV2CreateAndUpdateMeetingBodyDto requestBody,
		Principal principal);

	@Operation(summary = "일반 모임 지원")
	@ApiResponses(value = {@ApiResponse(responseCode = "201", description = "지원 완료"),
		@ApiResponse(responseCode = "400", description =
			"\"모임이 없습니다\" or \"기수/파트를 설정해주세요\" or \"정원이 꽉찼습니다\" or \"활동 기수가 아닙니다\" " +
				"or \"지원 가능한 파트가 아닙니다\" or \"지원 가능한 기간이 아닙니다\"", content = @Content),})
	ResponseEntity<MeetingV2ApplyMeetingResponseDto> applyGeneralMeeting(
		@RequestBody MeetingV2ApplyMeetingDto requestBody,
		Principal principal);

	@Operation(hidden = true, summary = "행사 모임 지원")
	@ApiResponses(value = {@ApiResponse(responseCode = "201", description = "지원 완료"),
		@ApiResponse(responseCode = "400", description =
			"\"모임이 없습니다\" or \"기수/파트를 설정해주세요\" or \"정원이 꽉찼습니다\" or \"활동 기수가 아닙니다\" " +
				"or \"지원 가능한 파트가 아닙니다\" or \"지원 가능한 기간이 아닙니다\"", content = @Content),})
	ResponseEntity<MeetingV2ApplyMeetingResponseDto> applyEventMeeting(
		@RequestBody MeetingV2ApplyMeetingDto requestBody,
		Principal principal);

	@Operation(summary = "모임 지원 취소")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "지원 취소 완료"),
		@ApiResponse(responseCode = "400", description =
			"존재하지 않는 모임 신청입니다.", content = @Content),})
	ResponseEntity<Void> applyMeetingCancel(@PathVariable Integer meetingId,
		Principal principal);

	@Operation(summary = "모임 지원자/참여자 조회", description = "모임 지원자/참여자 조회 (모임장이면 지원자, 아니면 참여자 조회)")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "모임 지원자/참여자 조회 성공"),
		@ApiResponse(responseCode = "400", description = "모임이 없습니다.", content = @Content),})
	ResponseEntity<MeetingGetApplyListResponseDto> findApplyList(@PathVariable Integer meetingId,
		@ModelAttribute MeetingGetAppliesQueryDto queryCommand,
		Principal principal);

	@Operation(summary = "모임 전체 조회/검색/필터링", description = "모임 전체 조회/검색/필터링\n")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "모임 지원자/참여자 조회 성공")})
	@Parameters({
		@Parameter(name = "page", description = "페이지, default = 1", example = "1", schema = @Schema(type = "integer", format = "int32")),
		@Parameter(name = "take", description = "가져올 데이터 개수, default = 12", example = "50", schema = @Schema(type = "integer", format = "int32")),
		@Parameter(name = "category", description = "카테고리", example = "스터디,번쩍", schema = @Schema(type = "string", format = "string")),
		@Parameter(name = "keyword", description = "키워드", example = "먹방,자기계발,기타", schema = @Schema(type = "string", format = "string")),
		@Parameter(name = "status", description = "모임 모집 상태", example = "0,1", schema = @Schema(type = "string", format = "string")),
		@Parameter(name = "isOnlyActiveGeneration", description = "활동기수만 참여여부", example = "true", schema = @Schema(type = "boolean", format = "boolean")),
		@Parameter(name = "joinableParts", description = "검색할 활동 파트 다중 선택. OR 조건으로 검색됨 </br> Available values : PM, DESIGN, IOS, ANDROID, SERVER, WEB", example = "PM,DESIGN,IOS,ANDROID,SERVER,WEB", schema = @Schema(type = "array[string]", format = "array[string]")),
		@Parameter(name = "query", description = "검색 내용", example = "고수스터디 검색", schema = @Schema(type = "string", format = "string")),
		@Parameter(name = "paginationType", description = "페이지네이션 타입", example = "ADVERTISEMENT", schema = @Schema(type = "string", format = "string"))
	})
	ResponseEntity<MeetingV2GetAllMeetingDto> getMeetings(@ModelAttribute MeetingV2GetAllMeetingQueryDto queryCommand,
		Principal principal);

	@Operation(summary = "모임 삭제", description = "모임 삭제합니다.")
	ResponseEntity<Void> deleteMeeting(@PathVariable Integer meetingId, Principal principal);

	@Operation(summary = "모임 수정", description = "모임 내용을 수정합니다.")
	ResponseEntity<Void> updateMeeting(@PathVariable Integer meetingId,
		@RequestBody @Valid MeetingV2CreateAndUpdateMeetingBodyDto requestBody, Principal principal);

	@Operation(summary = "모임 지원자 상태 변경", description = "모임 지원자의 지원 상태를 변경합니다.")
	ResponseEntity<Void> updateApplyStatus(@PathVariable Integer meetingId,
		@RequestBody @Valid ApplyV2UpdateStatusBodyDto requestBody, Principal principal);

	@Operation(summary = "Meeting 썸네일 업로드용 Pre-Signed URL 발급", description = "Meeting 썸네일 업로드용 Pre-Signed URL 발급합니다.")
	ResponseEntity<PreSignedUrlResponseDto> createPreSignedUrl(@PathParam("contentType") String contentType,
		Principal principal);

	@Operation(summary = "모임 지원자 목록 csv 파일 다운로드", description = "모임 지원자 목록 csv 파일 다운로드")
	@Parameters({
		@Parameter(name = "status", description = "0: 대기, 1: 승인된 신청자, 2: 거절된 신청자", example = "0,1", required = true, schema = @Schema(type = "string")),
		@Parameter(name = "type", description = "0: 지원, 1: 초대", example = "0,1", required = true, schema = @Schema(type = "string")),
		@Parameter(name = "order", description = "정렬순", example = "desc", schema = @Schema(type = "string", format = "string"))})
	ResponseEntity<AppliesCsvFileUrlResponseDto> getAppliesCsvFileUrl(
		@PathVariable Integer meetingId,
		@ModelAttribute @Valid @Parameter(hidden = true) MeetingGetAppliesCsvQueryDto queryCommand,
		Principal principal);

	@Operation(summary = "모임 상세 조회", description = "모임 상세 조회")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "모임 상세 조회 성공"),
		@ApiResponse(responseCode = "400", description = "모임이 없습니다.", content = @Content),})
	ResponseEntity<MeetingV2GetMeetingByIdResponseDto> getMeetingById(@PathVariable Integer meetingId,
		Principal principal);

	@Operation(summary = "추천 모임 목록 조회", description = "추천 모임 목록 조회, 쿼리파라미터가 없는 경우 '지금 모집중인 모임' 반환")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "추천 모임 목록 조회 성공"),
		@ApiResponse(responseCode = "400", description = "모임이 없습니다.", content = @Content)
	})
	ResponseEntity<MeetingV2GetRecommendDto> getRecommendMeetingsByIds(
		@RequestParam(name = "meetingIds", required = false) @Parameter(description = "추천할 모임들의 ID 리스트", example = "[101, 102, 103]") List<Integer> meetingIds,
		Principal principal);
}
