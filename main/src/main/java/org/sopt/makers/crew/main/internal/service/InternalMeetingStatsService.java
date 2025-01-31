package org.sopt.makers.crew.main.internal.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.sopt.makers.crew.main.entity.apply.Apply;
import org.sopt.makers.crew.main.entity.apply.ApplyRepository;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyStatus;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.internal.dto.ApprovedStudyCountResponseDto;
import org.sopt.makers.crew.main.internal.dto.TopFastestAppliedMeetingResponseDto;
import org.sopt.makers.crew.main.internal.dto.TopFastestAppliedMeetingsResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InternalMeetingStatsService {
	private final ApplyRepository applyRepository;
	private final UserRepository userRepository;

	public ApprovedStudyCountResponseDto getApprovedStudyCountByOrgId(Integer orgId) {
		User user = userRepository.findByOrgId(orgId).orElse(null);

		if (user == null) {
			return ApprovedStudyCountResponseDto.of(orgId, 0L);
		}

		Long approvedStudyCount = applyRepository.findApprovedStudyCountByOrgId(MeetingCategory.STUDY,
			EnApplyStatus.APPROVE, user.getOrgId());

		return ApprovedStudyCountResponseDto.of(user.getOrgId(), approvedStudyCount);
	}

	public TopFastestAppliedMeetingsResponseDto getTopFastestAppliedMeetings(Integer orgId, Integer queryCount, Integer queryYear) {
		Optional<User> user = userRepository.findByOrgId(orgId);

		if (user.isEmpty()) {
			return TopFastestAppliedMeetingsResponseDto.from(Collections.emptyList());
		}

		List<Apply> applies = applyRepository.findTopFastestAppliedMeetings(user.get().getId(), queryCount, queryYear);

		List<TopFastestAppliedMeetingResponseDto> responseDtos = applies.stream()
			.map(apply -> TopFastestAppliedMeetingResponseDto.from(apply.getMeeting()))
			.toList();

		return TopFastestAppliedMeetingsResponseDto.from(responseDtos);
	}
}
