package org.sopt.makers.crew.main.user.v2.service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.sopt.makers.crew.main.entity.meeting.CoLeaderRepository;
import org.sopt.makers.crew.main.entity.meeting.CoLeaders;
import org.sopt.makers.crew.main.global.exception.BaseException;
import org.sopt.makers.crew.main.global.util.Time;
import org.sopt.makers.crew.main.entity.apply.Applies;
import org.sopt.makers.crew.main.entity.apply.Apply;
import org.sopt.makers.crew.main.entity.apply.ApplyRepository;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyStatus;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.user.v2.dto.response.ApplyV2GetAppliedMeetingByUserResponseDto;
import org.sopt.makers.crew.main.user.v2.dto.response.MeetingV2GetCreatedMeetingByUserResponseDto;
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetAllMeetingByUserMeetingDto;
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetAllMentionUserDto;
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetAllUserDto;
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetAppliedMeetingByUserResponseDto;
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetCreatedMeetingByUserResponseDto;
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetUserOwnProfileResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserV2ServiceImpl implements UserV2Service {

	private final UserRepository userRepository;
	private final ApplyRepository applyRepository;
	private final MeetingRepository meetingRepository;
	private final CoLeaderRepository coLeaderRepository;

	private final Time time;

	@Override
	public List<UserV2GetAllMeetingByUserMeetingDto> getAllMeetingByUser(Integer userId) {
		User user = userRepository.findByIdOrThrow(userId);

		List<Meeting> myMeetings = meetingRepository.findAllByUserId(user.getId());

		List<UserV2GetAllMeetingByUserMeetingDto> userJoinedList = Stream.concat(myMeetings.stream(),
				applyRepository.findAllByUserIdAndStatus(userId, EnApplyStatus.APPROVE)
					.stream()
					.map(apply -> apply.getMeeting()))
			.map(meeting -> UserV2GetAllMeetingByUserMeetingDto.of(meeting.getId(), meeting.getTitle(),
				meeting.getDesc(), meeting.getImageURL().get(0).getUrl(), meeting.getCategory().getValue()))
			.sorted(Comparator.comparing(UserV2GetAllMeetingByUserMeetingDto::getId).reversed())
			.collect(Collectors.toList());

		if (userJoinedList.isEmpty()) {
			throw new BaseException(HttpStatus.NO_CONTENT);
		}
		return userJoinedList;
	}

	@Override
	public List<UserV2GetAllMentionUserDto> getAllMentionUser() {

		List<User> users = userRepository.findAll();

		return users.stream()
			.filter(user -> user.getActivities() != null)
			.map(user -> UserV2GetAllMentionUserDto.of(user.getOrgId(), user.getName(),
				user.getRecentActivityVO().getPart(), user.getRecentActivityVO().getGeneration(),
				user.getProfileImage()))
			.toList();
	}

	@Override
	public List<UserV2GetAllUserDto> getAllUser() {

		List<User> users = userRepository.findAll();

		return users.stream()
			.filter(user -> user.getActivities() != null)
			.map(UserV2GetAllUserDto::of)
			.toList();
	}

	@Override
	public UserV2GetUserOwnProfileResponseDto getUserOwnProfile(Integer userId) {
		User user = userRepository.findByIdOrThrow(userId);
		return UserV2GetUserOwnProfileResponseDto.of(user);
	}

	@Override
	public UserV2GetCreatedMeetingByUserResponseDto getCreatedMeetingByUser(Integer userId) {
		User meetingCreator = userRepository.findByIdOrThrow(userId);

		List<Meeting> meetings = meetingRepository.findAllByUser(meetingCreator);
		List<Integer> meetingIds = meetings.stream().map(Meeting::getId).toList();
		Applies applies = new Applies(applyRepository.findAllByMeetingIdIn(meetingIds));
		CoLeaders coLeaders = new CoLeaders(coLeaderRepository.findAllByMeetingIdIn(meetingIds));

		List<MeetingV2GetCreatedMeetingByUserResponseDto> meetingByUserDtos = meetings.stream()
			.map(meeting -> MeetingV2GetCreatedMeetingByUserResponseDto.of(meeting,
				coLeaders.isCoLeader(meeting.getId(), userId), meetingCreator,
				applies.getApprovedCount(meeting.getId()), time.now()))
			.toList();

		return UserV2GetCreatedMeetingByUserResponseDto.of(meetingByUserDtos);
	}

	@Override
	public UserV2GetAppliedMeetingByUserResponseDto getAppliedMeetingByUser(Integer userId) {
		List<Apply> myApplies = applyRepository.findAllByUserId(userId);
		List<Integer> meetingIds = myApplies.stream().map(Apply::getMeetingId).toList();

		Applies allApplies = new Applies(applyRepository.findAllByMeetingIdIn(meetingIds));

		List<ApplyV2GetAppliedMeetingByUserResponseDto> appliedMeetingByUserDtos = myApplies.stream()
			.map(apply -> ApplyV2GetAppliedMeetingByUserResponseDto.of(apply.getId(), apply.getStatus().getValue(),
				MeetingV2GetCreatedMeetingByUserResponseDto.of(apply.getMeeting(), false, apply.getMeeting().getUser(),
					allApplies.getApprovedCount(apply.getMeetingId()), time.now())))
			.toList();

		return UserV2GetAppliedMeetingByUserResponseDto.of(appliedMeetingByUserDtos);
	}

	@Override
	public User getUserByUserId(Integer userId) {
		return userRepository.findByIdOrThrow(userId);
	}
}
