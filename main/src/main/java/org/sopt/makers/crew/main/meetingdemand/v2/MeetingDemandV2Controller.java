package org.sopt.makers.crew.main.meetingdemand.v2;

import java.security.Principal;

import org.sopt.makers.crew.main.global.util.UserUtil;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.query.MeetingDemandV2GetMeetingDemandsQueryDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.request.MeetingDemandV2CreateMeetingDemandBodyDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.response.MeetingDemandV2CreateMeetingDemandResponseDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.response.MeetingDemandV2GetMeetingDemandResponseDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.response.MeetingDemandV2GetMeetingDemandsResponseDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.response.MeetingDemandV2ReportResponseDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.response.MeetingDemandV2SwitchMeetingDemandWaitResponseDto;
import org.sopt.makers.crew.main.meetingdemand.v2.service.MeetingDemandV2Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/meeting-demand/v2")
@RequiredArgsConstructor
public class MeetingDemandV2Controller implements MeetingDemandV2Api {

	private final MeetingDemandV2Service meetingDemandV2Service;

	@Override
	@GetMapping
	public ResponseEntity<MeetingDemandV2GetMeetingDemandsResponseDto> getMeetingDemands(
		@Valid @ModelAttribute @Parameter(hidden = true) MeetingDemandV2GetMeetingDemandsQueryDto queryDto,
		Principal principal) {
		Integer userId = UserUtil.getUserId(principal);
		return ResponseEntity.ok(meetingDemandV2Service.getMeetingDemands(queryDto, userId));
	}

	@Override
	@GetMapping("/{meetingDemandId}")
	public ResponseEntity<MeetingDemandV2GetMeetingDemandResponseDto> getMeetingDemand(
		@PathVariable Integer meetingDemandId, Principal principal) {
		Integer userId = UserUtil.getUserId(principal);
		return ResponseEntity.ok(meetingDemandV2Service.getMeetingDemand(meetingDemandId, userId));
	}

	@Override
	@PostMapping
	public ResponseEntity<MeetingDemandV2CreateMeetingDemandResponseDto> createMeetingDemand(
		@Valid @RequestBody MeetingDemandV2CreateMeetingDemandBodyDto requestBody, Principal principal) {
		Integer userId = UserUtil.getUserId(principal);
		return ResponseEntity.ok(meetingDemandV2Service.createMeetingDemand(requestBody, userId));
	}

	@Override
	@DeleteMapping("/{meetingDemandId}")
	public ResponseEntity<Void> deleteMeetingDemand(@PathVariable Integer meetingDemandId, Principal principal) {
		Integer userId = UserUtil.getUserId(principal);
		meetingDemandV2Service.deleteMeetingDemand(meetingDemandId, userId);
		return ResponseEntity.ok().build();
	}

	@Override
	@PostMapping("/{meetingDemandId}/wait")
	public ResponseEntity<MeetingDemandV2SwitchMeetingDemandWaitResponseDto> switchMeetingDemandWait(
		@PathVariable Integer meetingDemandId, Principal principal) {
		Integer userId = UserUtil.getUserId(principal);
		return ResponseEntity.ok(meetingDemandV2Service.switchMeetingDemandWait(meetingDemandId, userId));
	}

	@Override
	@PostMapping("/{meetingDemandId}/report")
	public ResponseEntity<MeetingDemandV2ReportResponseDto> reportMeetingDemand(
		@PathVariable Integer meetingDemandId, Principal principal) {
		Integer userId = UserUtil.getUserId(principal);
		return ResponseEntity.ok(meetingDemandV2Service.reportMeetingDemand(meetingDemandId, userId));
	}
}
