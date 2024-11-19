package org.sopt.makers.crew.main.internal.service;

import java.util.List;

import org.sopt.makers.crew.main.entity.apply.ApplyRepository;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyStatus;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.global.exception.ErrorStatus;
import org.sopt.makers.crew.main.global.exception.NotFoundException;
import org.sopt.makers.crew.main.internal.dto.ApprovedStudyCountProjection;
import org.sopt.makers.crew.main.internal.dto.ApprovedStudyCountResponseDto;
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
		User user = userRepository.findByOrgId(orgId)
			.orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_USER.getErrorCode()));

		return fetchApprovedStudyCount(user);
	}

	private ApprovedStudyCountResponseDto fetchApprovedStudyCount(User user) {
		List<ApprovedStudyCountProjection> results = applyRepository.findApprovedStudyCountByOrgId(
			MeetingCategory.STUDY, EnApplyStatus.APPROVE, user.getOrgId()
		);

		return results.stream()
			.findFirst()
			.map(ApprovedStudyCountResponseDto::fromProjection)
			.orElse(ApprovedStudyCountResponseDto.empty(user.getOrgId()));
	}
}
