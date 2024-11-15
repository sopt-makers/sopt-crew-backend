package org.sopt.makers.crew.main.internal;

import org.sopt.makers.crew.main.internal.dto.InternalMeetingGetAllMeetingDto;
import org.sopt.makers.crew.main.internal.dto.UserOrgIdRequestDto;
import org.sopt.makers.crew.main.internal.service.InternalMeetingService;
import org.sopt.makers.crew.main.meeting.v2.dto.query.MeetingV2GetAllMeetingQueryDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/internal/meetings")
@RequiredArgsConstructor
public class InternalMeetingController {
	private final InternalMeetingService internalMeetingService;

	@GetMapping
	public ResponseEntity<InternalMeetingGetAllMeetingDto> getMeetings(
		@ModelAttribute @Valid MeetingV2GetAllMeetingQueryDto queryCommand,
		@RequestBody @Valid UserOrgIdRequestDto orgIdRequestDto) {

		return ResponseEntity.ok().body(internalMeetingService.getMeetings(queryCommand, orgIdRequestDto));
	}
}
