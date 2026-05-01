package org.sopt.makers.crew.main.meeting.v2.service;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.sopt.makers.crew.main.entity.apply.Apply;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyStatus;
import org.sopt.makers.crew.main.entity.meeting.CoLeaders;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.enums.UserPart;
import org.sopt.makers.crew.main.entity.user.vo.UserActivityVO;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.global.util.ActiveGenerationProvider;
import org.sopt.makers.crew.main.global.util.Time;
import org.sopt.makers.crew.main.global.util.UserPartUtil;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MeetingApplyValidator {

	private final ActiveGenerationProvider activeGenerationProvider;
	private final Time time;

	public void validateGeneralApplyRequest(Meeting meeting, User user, Integer userId, List<Apply> applies,
		CoLeaders coLeaders) {
		validateMeetingCategoryNotEvent(meeting);
		validateCommonApplyRequest(meeting, user, userId, applies, coLeaders);
	}

	public void validateEventApplyRequest(Meeting meeting, User user, Integer userId, List<Apply> applies,
		CoLeaders coLeaders) {
		validateMeetingCategoryEvent(meeting);
		validateCommonApplyRequest(meeting, user, userId, applies, coLeaders);
	}

	public void validateMeetingCapacity(Meeting meeting, List<Apply> applies) {
		List<Apply> approvedApplies = applies.stream()
			.filter(apply -> apply.getStatus() == EnApplyStatus.APPROVE)
			.toList();

		meeting.validateCapacity(approvedApplies.size());
	}

	private void validateCommonApplyRequest(Meeting meeting, User user, Integer userId, List<Apply> applies,
		CoLeaders coLeaders) {
		validateMeetingCapacity(meeting, applies);
		validateUserAlreadyApplied(userId, applies);
		validateApplyPeriod(meeting);
		validateUserActivities(user);
		validateUserJoinableParts(user, meeting);
		coLeaders.validateCoLeader(meeting.getId(), user.getId());
		meeting.validateIsNotMeetingLeader(userId);
	}

	private void validateMeetingCategoryNotEvent(Meeting meeting) {
		if (meeting.getCategory() == MeetingCategory.EVENT) {
			throw new BadRequestException(INVALID_MEETING_CATEGORY.getErrorCode());
		}
	}

	private void validateMeetingCategoryEvent(Meeting meeting) {
		if (meeting.getCategory() != MeetingCategory.EVENT) {
			throw new BadRequestException(INVALID_MEETING_CATEGORY.getErrorCode());
		}
	}

	private void validateUserAlreadyApplied(Integer userId, List<Apply> applies) {
		boolean hasApplied = applies.stream()
			.anyMatch(appliedInfo -> appliedInfo.getUser().getId().equals(userId));

		if (hasApplied) {
			throw new BadRequestException(ALREADY_APPLIED_MEETING.getErrorCode());
		}
	}

	private void validateApplyPeriod(Meeting meeting) {
		LocalDateTime now = time.now();
		if (now.isAfter(meeting.getEndDate()) || now.isBefore(meeting.getStartDate())) {
			throw new BadRequestException(NOT_IN_APPLY_PERIOD.getErrorCode());
		}
	}

	private void validateUserActivities(User user) {
		if (user.getActivities() == null || user.getActivities().isEmpty()) {
			throw new BadRequestException(MISSING_GENERATION_PART.getErrorCode());
		}
	}

	private void validateUserJoinableParts(User user, Meeting meeting) {
		if (meeting.getJoinableParts().length == 0) {
			return;
		}

		List<UserActivityVO> userActivities = filterUserActivities(user, meeting);
		List<String> userJoinableParts = userActivities.stream()
			.map(UserActivityVO::getPart)
			.filter(part -> {
				MeetingJoinablePart meetingJoinablePart = UserPartUtil.getMeetingJoinablePart(
					UserPart.ofValue(part));

				if (meetingJoinablePart == null) {
					return true;
				}

				return Arrays.stream(meeting.getJoinableParts())
					.anyMatch(joinablePart -> joinablePart == meetingJoinablePart);
			})
			.collect(Collectors.toList());

		if (userJoinableParts.isEmpty()) {
			throw new BadRequestException(NOT_TARGET_PART.getErrorCode());
		}
	}

	private List<UserActivityVO> filterUserActivities(User user, Meeting meeting) {
		if (Objects.equals(meeting.getTargetActiveGeneration(), activeGenerationProvider.getActiveGeneration())
			&& meeting.getCanJoinOnlyActiveGeneration()) {
			List<UserActivityVO> filteredActivities = user.getActivities().stream()
				.filter(activity -> activity.getGeneration() == activeGenerationProvider.getActiveGeneration())
				.toList();

			if (filteredActivities.isEmpty()) {
				throw new BadRequestException(NOT_ACTIVE_GENERATION.getErrorCode());
			}

			return filteredActivities;
		}
		return user.getActivities();
	}
}
