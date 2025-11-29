package org.sopt.makers.crew.main.internal;

import org.sopt.makers.crew.main.global.pagination.dto.PageOptionsDto;
import org.sopt.makers.crew.main.internal.dto.InternalMeetingGetAllMeetingDto;
import org.sopt.makers.crew.main.internal.dto.InternalMeetingGetAllWritingPostResponseDto;
import org.sopt.makers.crew.main.internal.dto.InternalUserAppliedMeetingResponseDto;
import org.sopt.makers.crew.main.internal.service.InternalMeetingService;
import org.sopt.makers.crew.main.meeting.v2.dto.query.MeetingV2GetAllMeetingQueryDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
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

	@Override
	@GetMapping("/post")
	public ResponseEntity<InternalMeetingGetAllWritingPostResponseDto> getMeetingsForWritingPost(
		@ModelAttribute @Valid PageOptionsDto pageOptionsDto) {
		return ResponseEntity.ok().body(internalMeetingService.getMeetingsForWritingPost(pageOptionsDto));
	}

	@Override
	@GetMapping("/{userId}")
	public ResponseEntity<InternalUserAppliedMeetingResponseDto> getAppliedMeetingInfo(
		@PathVariable @Valid Integer userId) {
		return ResponseEntity.ok().body(
			InternalUserAppliedMeetingResponseDto.from(internalMeetingService.retrieveAppliedMeetingInfo(userId)));
	}
}
