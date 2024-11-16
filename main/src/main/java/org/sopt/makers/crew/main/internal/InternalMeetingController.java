package org.sopt.makers.crew.main.internal;

import org.sopt.makers.crew.main.internal.dto.InternalMeetingGetAllMeetingDto;
import org.sopt.makers.crew.main.internal.dto.UserOrgIdRequestDto;
import org.sopt.makers.crew.main.internal.service.InternalMeetingService;
import org.sopt.makers.crew.main.meeting.v2.dto.query.MeetingV2GetAllMeetingQueryDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/internal/meetings")
@RequiredArgsConstructor
public class InternalMeetingController implements InternalMeetingApi {
	private final InternalMeetingService internalMeetingService;

	@Override
	@GetMapping
	public ResponseEntity<InternalMeetingGetAllMeetingDto> getMeetings(
		@ModelAttribute @Valid MeetingV2GetAllMeetingQueryDto queryCommand,
		@RequestParam @Valid Integer orgId) {

		return ResponseEntity.ok().body(internalMeetingService.getMeetings(queryCommand, orgId));
	}
}
