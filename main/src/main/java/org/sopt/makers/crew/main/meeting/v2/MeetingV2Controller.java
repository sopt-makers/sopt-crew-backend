package org.sopt.makers.crew.main.meeting.v2;

import java.security.Principal;
import java.util.List;

import org.sopt.makers.crew.main.external.s3.service.S3Service;
import org.sopt.makers.crew.main.global.util.UserUtil;
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
import org.sopt.makers.crew.main.meeting.v2.service.MeetingV2Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/meeting/v2")
@RequiredArgsConstructor
public class MeetingV2Controller implements MeetingV2Api {

	private final MeetingV2Service meetingV2Service;

	private final S3Service s3Service;

	@Override
	@GetMapping("/org-user")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<MeetingV2GetAllMeetingByOrgUserDto> getAllMeetingByOrgUser(
		@ModelAttribute @Parameter(hidden = true) MeetingV2GetAllMeetingByOrgUserQueryDto queryDto) {
		return ResponseEntity.ok(meetingV2Service.getAllMeetingByOrgUser(queryDto));
	}

	@Override
	@GetMapping("/banner")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<List<MeetingV2GetMeetingBannerResponseDto>> getMeetingBanner(
		Principal principal) {
		UserUtil.getUserId(principal);
		return ResponseEntity.ok(meetingV2Service.getMeetingBanner());
	}

	@Override
	@PostMapping
	public ResponseEntity<MeetingV2CreateMeetingResponseDto> createMeeting(
		@Valid @RequestBody MeetingV2CreateAndUpdateMeetingBodyDto requestBody,
		Principal principal) {
		Integer userId = UserUtil.getUserId(principal);
		return ResponseEntity.status(HttpStatus.CREATED).body(meetingV2Service.createMeeting(requestBody, userId));
	}

	@Override
	@PostMapping("/apply")
	public ResponseEntity<MeetingV2ApplyMeetingResponseDto> applyGeneralMeeting(
		@Valid @RequestBody MeetingV2ApplyMeetingDto requestBody,
		Principal principal) {
		Integer userId = UserUtil.getUserId(principal);
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(meetingV2Service.applyGeneralMeetingWithLock(requestBody, userId));
	}

	@PostMapping("${custom.paths.eventApply}")
	public ResponseEntity<MeetingV2ApplyMeetingResponseDto> applyEventMeeting(
		@Valid @RequestBody MeetingV2ApplyMeetingDto requestBody,
		Principal principal) {
		Integer userId = UserUtil.getUserId(principal);
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(meetingV2Service.applyEventMeetingWithLock(requestBody, userId));
	}

	@Override
	@DeleteMapping("/{meetingId}/apply")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<Void> applyMeetingCancel(@PathVariable Integer meetingId,
		Principal principal) {
		Integer userId = UserUtil.getUserId(principal);
		meetingV2Service.applyMeetingCancel(meetingId, userId);
		return ResponseEntity.status(HttpStatus.OK).build();
	}

	@Override
	@GetMapping("/{meetingId}/list")
	public ResponseEntity<MeetingGetApplyListResponseDto> findApplyList(@PathVariable Integer meetingId,
		@ModelAttribute MeetingGetAppliesQueryDto queryCommand,
		Principal principal) {

		Integer userId = UserUtil.getUserId(principal);

		return ResponseEntity.status(HttpStatus.OK)
			.body(meetingV2Service.findApplyList(queryCommand, meetingId, userId));
	}

	@Override
	@GetMapping
	public ResponseEntity<MeetingV2GetAllMeetingDto> getMeetings(
		@ModelAttribute @Valid MeetingV2GetAllMeetingQueryDto queryCommand,
		Principal principal) {

		MeetingV2GetAllMeetingDto meetings = meetingV2Service.getMeetings(queryCommand);
		return ResponseEntity.ok().body(meetings);
	}

	@Override
	@DeleteMapping("/{meetingId}")
	public ResponseEntity<Void> deleteMeeting(@PathVariable Integer meetingId, Principal principal) {
		Integer userId = UserUtil.getUserId(principal);
		meetingV2Service.deleteMeeting(meetingId, userId);

		return ResponseEntity.ok().build();
	}

	@Override
	@PutMapping("/{meetingId}")
	public ResponseEntity<Void> updateMeeting(
		@PathVariable Integer meetingId,
		@RequestBody @Valid MeetingV2CreateAndUpdateMeetingBodyDto requestBody,
		Principal principal) {

		Integer userId = UserUtil.getUserId(principal);
		meetingV2Service.updateMeeting(meetingId, requestBody, userId);

		return ResponseEntity.ok().build();
	}

	@Override
	@PutMapping("/{meetingId}/apply/status")
	public ResponseEntity<Void> updateApplyStatus(
		@PathVariable Integer meetingId,
		@RequestBody @Valid ApplyV2UpdateStatusBodyDto requestBody,
		Principal principal) {

		Integer userId = UserUtil.getUserId(principal);
		meetingV2Service.updateApplyStatus(meetingId, requestBody, userId);

		return ResponseEntity.ok().build();
	}

	@Override
	@GetMapping("/presigned-url")
	public ResponseEntity<PreSignedUrlResponseDto> createPreSignedUrl(
		@RequestParam("contentType") String contentType, Principal principal) {
		PreSignedUrlResponseDto responseDto = s3Service.generatePreSignedUrl(contentType);

		return ResponseEntity.ok(responseDto);
	}

	@Override
	@GetMapping("/{meetingId}/list/csv")
	public ResponseEntity<AppliesCsvFileUrlResponseDto> getAppliesCsvFileUrl(
		@PathVariable Integer meetingId,
		@ModelAttribute @Valid MeetingGetAppliesCsvQueryDto queryCommand,
		Principal principal) {

		Integer userId = UserUtil.getUserId(principal);

		// TODO: FE 에서 request 값 변경하도록 요청 필요
		List<Integer> statuses = List.of(0, 1, 2);

		AppliesCsvFileUrlResponseDto responseDto = meetingV2Service.getAppliesCsvFileUrl(meetingId, statuses,
			queryCommand.getOrder(), userId);

		return ResponseEntity.ok(responseDto);
	}

	@Override
	@GetMapping("/{meetingId}")
	public ResponseEntity<MeetingV2GetMeetingByIdResponseDto> getMeetingById(@PathVariable Integer meetingId,
		Principal principal) {
		Integer userId = UserUtil.getUserId(principal);

		return ResponseEntity.ok(meetingV2Service.getMeetingDetail(meetingId, userId));
	}

	@Override
	@GetMapping("/recommend")
	public ResponseEntity<MeetingV2GetRecommendDto> getRecommendMeetingsByIds(
		@RequestParam(name = "meetingIds", required = false) List<Integer> meetingIds,
		Principal principal) {
		Integer userId = UserUtil.getUserId(principal);

		return ResponseEntity.ok().body(meetingV2Service.getRecommendMeetingsByIds(meetingIds, userId));
	}
}
