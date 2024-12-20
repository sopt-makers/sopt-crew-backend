package org.sopt.makers.crew.main.internal;

import org.sopt.makers.crew.main.internal.dto.ApprovedStudyCountResponseDto;
import org.sopt.makers.crew.main.internal.service.InternalMeetingStatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/internal/meeting/stats")
@RequiredArgsConstructor
public class InternalMeetingStatsController {
	private final InternalMeetingStatsService internalMeetingStatsService;

	@GetMapping("/approved-studies/{orgId}")
	public ResponseEntity<ApprovedStudyCountResponseDto> getApprovedStudyCountByOrgId(
		@PathVariable Integer orgId
	) {
		ApprovedStudyCountResponseDto response = internalMeetingStatsService.getApprovedStudyCountByOrgId(orgId);
		return ResponseEntity.ok(response);
	}
}
