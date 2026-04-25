package org.sopt.makers.crew.main.meeting.v2.service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.sopt.makers.crew.main.entity.apply.Apply;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.vo.UserActivityVO;
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
		UserActivityVO requestUserActivity = getRequestUserActivity(requestUser);
		int activeGeneration = activeGenerationProvider.getActiveGeneration();
		if (requestUserActivity == null) {
			return MeetingV2ParticipatingPartInfoDto.of(null, 0, false, activeGeneration, List.of());
		}

		boolean isActiveGeneration = isActiveGenerationUser(requestUser, activeGeneration);
		String requestUserPart = requestUserActivity.getPart();
		if (!isActiveGeneration) {
			Set<Integer> honoraryGenerations = getHonoraryGenerations(requestUser, activeGeneration);
			List<String> memberNames = participatingApplies.stream()
				.filter(apply -> isSameHonoraryGenerationParticipatingApply(apply, honoraryGenerations))
				.map(apply -> apply.getUser().getName())
				.toList();
			return MeetingV2ParticipatingPartInfoDto.of(requestUserPart, memberNames.size(), false, activeGeneration,
				memberNames);
		}

		String normalizedRequestUserPart = meetingPartNormalizer.normalize(requestUserPart);
		List<String> memberNames = participatingApplies.stream()
			.filter(apply -> isSamePartParticipatingApply(apply, normalizedRequestUserPart))
			.map(apply -> apply.getUser().getName())
			.toList();

		return MeetingV2ParticipatingPartInfoDto.of(requestUserPart, memberNames.size(), true, activeGeneration,
			memberNames);
	}

	public MeetingV2GetMeetingPartMembersResponseDto createMeetingPartMembersResponse(User requestUser,
		List<Apply> participatingApplies) {
		UserActivityVO requestUserActivity = getRequestUserActivity(requestUser);
		if (requestUserActivity == null) {
			return MeetingV2GetMeetingPartMembersResponseDto.of(null, 0, List.of());
		}

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

	private boolean isSameHonoraryGenerationParticipatingApply(Apply apply, Set<Integer> honoraryGenerations) {
		if (honoraryGenerations.isEmpty() || apply.getUser().getActivities() == null) {
			return false;
		}

		return apply.getUser().getActivities().stream()
			.anyMatch(userActivityVO -> honoraryGenerations.contains(userActivityVO.getGeneration()));
	}

	private boolean isActiveGenerationUser(User requestUser, int activeGeneration) {
		if (requestUser.getActivities() == null || requestUser.getActivities().isEmpty()) {
			return false;
		}

		return requestUser.getActivities().stream()
			.anyMatch(userActivityVO -> userActivityVO.getGeneration() == activeGeneration);
	}

	private Set<Integer> getHonoraryGenerations(User requestUser, int activeGeneration) {
		if (requestUser.getActivities() == null) {
			return Set.of();
		}

		return requestUser.getActivities().stream()
			.map(UserActivityVO::getGeneration)
			.filter(generation -> generation != activeGeneration)
			.collect(Collectors.toSet());
	}

	private UserActivityVO getRequestUserActivity(User requestUser) {
		if (requestUser.getActivities() == null || requestUser.getActivities().isEmpty()) {
			return null;
		}

		return requestUser.getActivities().stream()
			.filter(userActivityVO -> userActivityVO.getGeneration() == activeGenerationProvider.getActiveGeneration())
			.findFirst()
			.orElseGet(requestUser::getRecentActivityVO);
	}

	private UserActivityVO getParticipatingUserActivity(User participatingUser) {
		if (participatingUser.getActivities() == null || participatingUser.getActivities().isEmpty()) {
			return null;
		}

		return participatingUser.getRecentActivityVO();
	}
}
