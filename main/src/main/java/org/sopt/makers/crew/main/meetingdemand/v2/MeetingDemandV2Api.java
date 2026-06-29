package org.sopt.makers.crew.main.meetingdemand.v2;

import java.security.Principal;

import org.sopt.makers.crew.main.meetingdemand.v2.dto.query.MeetingDemandV2GetMeetingDemandsQueryDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.request.MeetingDemandV2CreateMeetingDemandBodyDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.response.MeetingDemandV2CreateMeetingDemandResponseDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.response.MeetingDemandV2GetMeetingDemandResponseDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.response.MeetingDemandV2GetMeetingDemandsResponseDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.response.MeetingDemandV2SwitchMeetingDemandWaitResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "모임 수요")
public interface MeetingDemandV2Api {

	@Operation(summary = "모임 수요 리스트 조회", description = "개설 전 상태의 모임 수요 목록을 최신순으로 조회")
	@ApiResponse(responseCode = "200", description = "성공")
	@Parameters({
		@Parameter(name = "page", description = "페이지, default = 1", example = "1", schema = @Schema(type = "integer", format = "int32")),
		@Parameter(name = "take", description = "가져올 데이터 개수, default = 3", example = "3", schema = @Schema(type = "integer", format = "int32"))
	})
	ResponseEntity<MeetingDemandV2GetMeetingDemandsResponseDto> getMeetingDemands(
		@Valid @ModelAttribute @Parameter(hidden = true) MeetingDemandV2GetMeetingDemandsQueryDto queryDto,
		Principal principal);

	@Operation(summary = "모임 수요 상세 조회", description = "모임 수요의 상세 정보 조회")
	@ApiResponse(responseCode = "200", description = "성공")
	ResponseEntity<MeetingDemandV2GetMeetingDemandResponseDto> getMeetingDemand(
		@PathVariable Integer meetingDemandId, Principal principal);

	@Operation(summary = "모임 수요 제안하기", description = "모임 수요 생성 API")
	@ApiResponse(responseCode = "200", description = "성공")
	ResponseEntity<MeetingDemandV2CreateMeetingDemandResponseDto> createMeetingDemand(
		@Valid @RequestBody MeetingDemandV2CreateMeetingDemandBodyDto requestBody, Principal principal);

	@Operation(summary = "모임 수요 삭제", description = "모임 수요 삭제 API")
	@ApiResponse(responseCode = "200", description = "성공")
	ResponseEntity<Void> deleteMeetingDemand(@PathVariable Integer meetingDemandId, Principal principal);

	@Operation(summary = "모임 수요 기다려요 토글", description = "모임 수요의 기다려요 상태를 토글합니다.")
	@ApiResponse(responseCode = "200", description = "성공")
	ResponseEntity<MeetingDemandV2SwitchMeetingDemandWaitResponseDto> switchMeetingDemandWait(
		@PathVariable Integer meetingDemandId, Principal principal);
}
