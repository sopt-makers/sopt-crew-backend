package org.sopt.makers.crew.main.internal;

import org.sopt.makers.crew.main.internal.dto.ApprovedStudyCountResponseDto;
import org.sopt.makers.crew.main.internal.dto.InternalMeetingCountResponseDto;
import org.sopt.makers.crew.main.internal.dto.InternalPostLikeRequestDto;
import org.sopt.makers.crew.main.internal.dto.InternalPostLikeResponseDto;
import org.sopt.makers.crew.main.internal.dto.TopFastestAppliedMeetingsResponseDto;
import org.sopt.makers.crew.main.internal.service.InternalMeetingStatsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/internal/meeting/stats")
@RequiredArgsConstructor
public class InternalMeetingStatsController implements InternalMeetingStatsApi {
	private final InternalMeetingStatsService internalMeetingStatsService;

	@Override
	@GetMapping("/approved-studies/{orgId}")
	public ResponseEntity<ApprovedStudyCountResponseDto> getApprovedStudyCountByOrgId(
		@PathVariable Integer orgId
	) {
		ApprovedStudyCountResponseDto response = internalMeetingStatsService.getApprovedStudyCountByOrgId(orgId);
		return ResponseEntity.ok(response);
	}

	@Override
	@GetMapping("/fastest-applied/{orgId}")
	public ResponseEntity<TopFastestAppliedMeetingsResponseDto> getTopFastestAppliedMeetings(
		@PathVariable Integer orgId,
		@RequestParam(name = "query-count", required = false, defaultValue = "3") Integer queryCount,
		@RequestParam(name = "query-year") Integer queryYear) {

		TopFastestAppliedMeetingsResponseDto response = internalMeetingStatsService.getTopFastestAppliedMeetings(
			orgId, queryCount, queryYear);
		return ResponseEntity.ok(response);
	}

	@Override
	@GetMapping("/studies")
	public ResponseEntity<InternalMeetingCountResponseDto> getMeetingCountByGeneration(
		@RequestParam(name = "generation") Integer generation) {

		InternalMeetingCountResponseDto response = internalMeetingStatsService.getMeetingCountByGeneration(
			generation);
		return ResponseEntity.ok(response);
	}

	@Override
	@PostMapping("/likes")
	public ResponseEntity<InternalPostLikeResponseDto> switchPostLike(
		@Valid @RequestBody InternalPostLikeRequestDto requestDto) {

		InternalPostLikeResponseDto response = internalMeetingStatsService.switchPostLike(requestDto);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}
