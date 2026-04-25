package org.sopt.makers.crew.main.meeting.v2.service;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
		UserActivityVO requestUserActivity = getRequiredRequestUserActivity(requestUser);
		int latestActiveGeneration = activeGenerationProvider.getActiveGeneration();
		boolean isActiveGeneration = isActiveGenerationUser(requestUser, latestActiveGeneration);
		String requestUserPart = requestUserActivity.getPart();
		if (!isActiveGeneration) {
			Integer latestHonoraryGeneration = getLatestHonoraryGeneration(requestUser, latestActiveGeneration)
				.orElseThrow(() -> new BadRequestException(MISSING_GENERATION_PART.getErrorCode()));
			List<String> memberNames = participatingApplies.stream()
				.filter(apply -> isSameHonoraryGenerationParticipatingApply(apply, latestHonoraryGeneration))
				.map(apply -> apply.getUser().getName())
				.toList();
			return MeetingV2ParticipatingPartInfoDto.of(requestUserPart, memberNames.size(), false,
				latestHonoraryGeneration,
				memberNames);
		}

		String normalizedRequestUserPart = meetingPartNormalizer.normalize(requestUserPart);
		List<String> memberNames = participatingApplies.stream()
			.filter(apply -> isSamePartParticipatingApply(apply, normalizedRequestUserPart))
			.map(apply -> apply.getUser().getName())
			.toList();

		return MeetingV2ParticipatingPartInfoDto.of(requestUserPart, memberNames.size(), true, latestActiveGeneration,
			memberNames);
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

	private boolean isSameHonoraryGenerationParticipatingApply(Apply apply, int honoraryGeneration) {
		if (apply.getUser().getActivities() == null) {
			return false;
		}

		return apply.getUser().getActivities().stream()
			.anyMatch(userActivityVO -> userActivityVO.getGeneration() == honoraryGeneration);
	}

	private boolean isActiveGenerationUser(User requestUser, int activeGeneration) {
		if (requestUser.getActivities() == null || requestUser.getActivities().isEmpty()) {
			return false;
		}

		return requestUser.getActivities().stream()
			.anyMatch(userActivityVO -> userActivityVO.getGeneration() == activeGeneration);
	}

	private Optional<Integer> getLatestHonoraryGeneration(User requestUser, int activeGeneration) {
		if (requestUser.getActivities() == null) {
			return Optional.empty();
		}

		return requestUser.getActivities().stream()
			.map(UserActivityVO::getGeneration)
			.filter(generation -> generation != activeGeneration)
			.max(Integer::compareTo);
	}

	private UserActivityVO getRequiredRequestUserActivity(User requestUser) {
		if (requestUser.getActivities() == null || requestUser.getActivities().isEmpty()) {
			throw new BadRequestException(MISSING_GENERATION_PART.getErrorCode());
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
