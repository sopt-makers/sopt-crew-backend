package org.sopt.makers.crew.main.user.v2.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.sopt.makers.crew.main.entity.apply.Applies;
import org.sopt.makers.crew.main.entity.apply.Apply;
import org.sopt.makers.crew.main.entity.apply.ApplyRepository;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyStatus;
import org.sopt.makers.crew.main.entity.meeting.CoLeader;
import org.sopt.makers.crew.main.entity.meeting.CoLeaderRepository;
import org.sopt.makers.crew.main.entity.meeting.CoLeaders;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;
import org.sopt.makers.crew.main.entity.tag.enums.MeetingKeywordType;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.global.exception.BaseException;
import org.sopt.makers.crew.main.global.util.ActiveGenerationProvider;
import org.sopt.makers.crew.main.global.util.Time;
import org.sopt.makers.crew.main.tag.v2.dto.response.TagV2MeetingTagsResponseDto;
import org.sopt.makers.crew.main.tag.v2.service.TagV2Service;
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

	private final TagV2Service tagV2Service;
	private final UserRepository userRepository;
	private final ApplyRepository applyRepository;
	private final MeetingRepository meetingRepository;
	private final CoLeaderRepository coLeaderRepository;

	private final ActiveGenerationProvider activeGenerationProvider;
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

	/**
	 * @implSpec : 유저가 모임장이거나 공동모임장인 모임을 모두 조회한다.
	 * @implNote : my 의미 == 내가 모임장이거나 공동모임장인 경우
	 **/
	@Override
	public UserV2GetCreatedMeetingByUserResponseDto getCreatedMeetingByUser(Integer userId) {
		List<Integer> coLeaderMeetingIds = getCoLeaderMeetingIds(coLeaderRepository.findAllByUserId(userId));

		List<Meeting> myMeetings = meetingRepository.findAllByUserIdOrIdInWithUser(userId, coLeaderMeetingIds);
		List<Integer> myMeetingIds = myMeetings.stream()
			.map(Meeting::getId)
			.toList();

		Applies applies = new Applies(applyRepository.findAllByMeetingIdIn(myMeetingIds));
		CoLeaders coLeaders = new CoLeaders(coLeaderRepository.findAllByMeetingIdIn(myMeetingIds));

		Map<Integer, TagV2MeetingTagsResponseDto> allTagsResponseDto = tagV2Service.getMeetingTagsByMeetingIds(
			myMeetingIds);

		List<MeetingV2GetCreatedMeetingByUserResponseDto> meetingByUserDtos = myMeetings.stream()
			.map(meeting -> MeetingV2GetCreatedMeetingByUserResponseDto.of(
				meeting,
				coLeaders.isCoLeader(meeting.getId(), userId),
				applies.getApprovedCount(meeting.getId()),
				time.now(),
				activeGenerationProvider.getActiveGeneration(),
				allTagsResponseDto)
			)
			.toList();

		return UserV2GetCreatedMeetingByUserResponseDto.from(meetingByUserDtos);
	}

	@Override
	public UserV2GetAppliedMeetingByUserResponseDto getAppliedMeetingByUser(Integer userId) {
		List<Apply> myApplies = applyRepository.findAllByUserIdOrderByIdDesc(userId);
		List<Integer> meetingIds = myApplies.stream()
			.map(Apply::getMeetingId)
			.toList();

		Applies allApplies = new Applies(applyRepository.findAllByMeetingIdIn(meetingIds));

		Map<Integer, TagV2MeetingTagsResponseDto> allTagsResponseDto = tagV2Service.getMeetingTagsByMeetingIds(
			meetingIds);

		List<ApplyV2GetAppliedMeetingByUserResponseDto> appliedMeetingByUserDtos = myApplies.stream()
			.map(apply -> ApplyV2GetAppliedMeetingByUserResponseDto.of(
				apply.getId(),
				apply.getStatus().getValue(),
				MeetingV2GetCreatedMeetingByUserResponseDto.of(
					apply.getMeeting(),
					false,
					allApplies.getApprovedCount(apply.getMeetingId()),
					time.now(),
					activeGenerationProvider.getActiveGeneration(),
					allTagsResponseDto))
			)
			.toList();

		return UserV2GetAppliedMeetingByUserResponseDto.from(appliedMeetingByUserDtos);
	}

	@Override
	public User getUserByUserId(Integer userId) {
		return userRepository.findByIdOrThrow(userId);
	}

	@Override
	@Transactional
	public void updateInterestedKeywords(Integer userId, List<String> keywords) {
		User user = userRepository.findByIdOrThrow(userId);
		List<MeetingKeywordType> updateKeywords = keywords.stream()
			.map(MeetingKeywordType::valueOf)
			.toList();
		user.updateKeywords(updateKeywords);
	}

	private List<Integer> getCoLeaderMeetingIds(List<CoLeader> coLeaders) {
		return coLeaders.stream()
			.map(coLeader -> coLeader.getMeeting().getId()).toList();
	}
}
