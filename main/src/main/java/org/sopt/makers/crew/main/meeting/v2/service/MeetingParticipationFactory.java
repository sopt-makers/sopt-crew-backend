package org.sopt.makers.crew.main.meeting.v2.service;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.sopt.makers.crew.main.entity.apply.Apply;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.vo.UserActivityVO;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.global.util.ActiveGenerationProvider;
import org.sopt.makers.crew.main.meeting.v2.dto.response.ApplyMemberInfoDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2GetMeetingPartMembersResponseDto;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MeetingParticipationFactory {

	private final ActiveGenerationProvider activeGenerationProvider;
	private final MeetingPartNormalizer meetingPartNormalizer;

	public MeetingV2GetMeetingPartMembersResponseDto createMeetingPartMembersResponse(User requestUser,
		List<Apply> participatingApplies) {
		int activeGeneration = activeGenerationProvider.getActiveGeneration();
		UserActivityVO requestUserActivity = getRequiredRequestUserActivity(requestUser, activeGeneration);
		boolean isActiveGeneration = isActiveGenerationUser(requestUser, activeGeneration);
		String requestUserPart = requestUserActivity.getPart();
		List<Apply> participatingPartApplies = getParticipatingPartApplies(participatingApplies, requestUserActivity,
			isActiveGeneration);
		AtomicInteger applyNumber = new AtomicInteger(1);
		List<ApplyMemberInfoDto> appliedInfo = participatingPartApplies.stream()
			.map(apply -> ApplyMemberInfoDto.of(apply, applyNumber.getAndIncrement()))
			.toList();

		return MeetingV2GetMeetingPartMembersResponseDto.of(requestUserPart, participatingPartApplies.size(),
			isActiveGeneration, requestUserActivity.getGeneration(), appliedInfo);
	}

	private List<Apply> getParticipatingPartApplies(List<Apply> participatingApplies,
		UserActivityVO requestUserActivity, boolean isActiveGeneration) {
		return participatingApplies.stream()
			.filter(apply -> isParticipatingPartMember(apply, requestUserActivity, isActiveGeneration))
			.toList();
	}

	private boolean isParticipatingPartMember(Apply apply, UserActivityVO requestUserActivity,
		boolean isActiveGeneration) {
		if (apply.getUser().getActivities() == null || apply.getUser().getActivities().isEmpty()) {
			return false;
		}

		if (!isActiveGeneration) {
			return isSameGenerationParticipatingApply(apply, requestUserActivity.getGeneration());
		}

		String normalizedRequestUserPart = meetingPartNormalizer.normalize(requestUserActivity.getPart());
		return apply.getUser().getActivities().stream()
			.anyMatch(activity -> isSameGenerationAndPartActivity(activity, requestUserActivity.getGeneration(),
				normalizedRequestUserPart));
	}

	private boolean isSameGenerationParticipatingApply(Apply apply, int generation) {
		return apply.getUser().getActivities().stream()
			.anyMatch(userActivityVO -> userActivityVO.getGeneration() == generation);
	}

	private boolean isSameGenerationAndPartActivity(UserActivityVO activity, int generation,
		String normalizedRequestUserPart) {
		String normalizedParticipatingUserPart = meetingPartNormalizer.normalize(activity.getPart());
		return activity.getGeneration() == generation
			&& Objects.equals(normalizedParticipatingUserPart, normalizedRequestUserPart);
	}

	private boolean isActiveGenerationUser(User requestUser, int activeGeneration) {
		if (requestUser.getActivities() == null || requestUser.getActivities().isEmpty()) {
			return false;
		}

		return requestUser.getActivities().stream()
			.anyMatch(userActivityVO -> userActivityVO.getGeneration() == activeGeneration);
	}

	private UserActivityVO getRequiredRequestUserActivity(User requestUser, int activeGeneration) {
		if (requestUser.getActivities() == null || requestUser.getActivities().isEmpty()) {
			throw new BadRequestException(MISSING_GENERATION_PART.getErrorCode());
		}

		return requestUser.getActivities().stream()
			.filter(userActivityVO -> userActivityVO.getGeneration() == activeGeneration)
			.findFirst()
			.orElseGet(requestUser::getRecentActivityVO);
	}
}
