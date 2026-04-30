package org.sopt.makers.crew.main.meeting.v2.service;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.util.List;
import java.util.Objects;

import org.sopt.makers.crew.main.entity.apply.Apply;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.vo.UserActivityVO;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.global.util.ActiveGenerationProvider;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2GetMeetingPartMembersResponseDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2ParticipatingPartInfoDto;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MeetingParticipationFactory {

	private final ActiveGenerationProvider activeGenerationProvider;
	private final MeetingPartNormalizer meetingPartNormalizer;

	public MeetingV2ParticipatingPartInfoDto createParticipatingPartInfo(User requestUser,
		List<Apply> participatingApplies) {
		int activeGeneration = activeGenerationProvider.getActiveGeneration();
		UserActivityVO requestUserActivity = getRequiredRequestUserActivity(requestUser, activeGeneration);
		boolean isActiveGeneration = isActiveGenerationUser(requestUser, activeGeneration);
		String requestUserPart = requestUserActivity.getPart();
		List<String> memberNames = getParticipatingPartMemberNames(participatingApplies, requestUserActivity,
			isActiveGeneration);

		return MeetingV2ParticipatingPartInfoDto.of(requestUserPart, memberNames.size(), isActiveGeneration,
			requestUserActivity.getGeneration(), memberNames);
	}

	public MeetingV2GetMeetingPartMembersResponseDto createMeetingPartMembersResponse(User requestUser,
		List<Apply> participatingApplies) {
		UserActivityVO requestUserActivity = getRequiredRequestUserActivity(requestUser);
		String requestUserPart = requestUserActivity.getPart();
		String normalizedRequestUserPart = meetingPartNormalizer.normalize(requestUserPart);
		List<String> memberNames = participatingApplies.stream()
			.filter(apply -> isSamePartParticipatingApply(apply, normalizedRequestUserPart))
			.map(apply -> apply.getUser().getName())
			.toList();

		return MeetingV2GetMeetingPartMembersResponseDto.of(requestUserPart, memberNames.size(), memberNames);
	}

	private boolean isSamePartParticipatingApply(Apply apply, String normalizedRequestUserPart) {
		UserActivityVO participatingUserActivity = getParticipatingUserActivity(apply.getUser());
		if (participatingUserActivity == null) {
			return false;
		}

		String normalizedParticipatingUserPart = meetingPartNormalizer.normalize(participatingUserActivity.getPart());
		return Objects.equals(normalizedParticipatingUserPart, normalizedRequestUserPart);
	}

	private List<String> getParticipatingPartMemberNames(List<Apply> participatingApplies,
		UserActivityVO requestUserActivity, boolean isActiveGeneration) {
		return participatingApplies.stream()
			.filter(apply -> isParticipatingPartMember(apply, requestUserActivity, isActiveGeneration))
			.map(apply -> apply.getUser().getName())
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

	private UserActivityVO getRequiredRequestUserActivity(User requestUser) {
		return getRequiredRequestUserActivity(requestUser, activeGenerationProvider.getActiveGeneration());
	}

	private UserActivityVO getParticipatingUserActivity(User participatingUser) {
		if (participatingUser.getActivities() == null || participatingUser.getActivities().isEmpty()) {
			return null;
		}

		return participatingUser.getRecentActivityVO();
	}
}
