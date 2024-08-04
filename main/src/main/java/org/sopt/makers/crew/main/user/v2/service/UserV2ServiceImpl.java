package org.sopt.makers.crew.main.user.v2.service;

import static org.sopt.makers.crew.main.common.response.ErrorStatus.NOT_FOUND_USER;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.crew.main.common.exception.BaseException;
import org.sopt.makers.crew.main.common.exception.NotFoundException;
import org.sopt.makers.crew.main.entity.apply.Apply;
import org.sopt.makers.crew.main.entity.apply.ApplyRepository;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyStatus;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetAllMeetingByUserMeetingDto;
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetAllMentionUserDto;
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetAppliedMeetingByUserResponseDto;
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetCreatedMeetingByUserResponseDto;
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetUserOwnProfileResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserV2ServiceImpl implements UserV2Service {

  private final UserRepository userRepository;
  private final ApplyRepository applyRepository;
  private final MeetingRepository meetingRepository;


  @Override
  public List<UserV2GetAllMeetingByUserMeetingDto> getAllMeetingByUser(Integer userId) {
    User user = userRepository.findByIdOrThrow(userId);

    List<Meeting> myMeetings = meetingRepository.findAllByUserId(user.getId());

    List<UserV2GetAllMeetingByUserMeetingDto> userJoinedList = Stream.concat(
            myMeetings.stream(),
            applyRepository.findAllByUserIdAndStatus(userId, EnApplyStatus.APPROVE)
                .stream()
                .map(apply -> apply.getMeeting())
        )
        .map(meeting -> UserV2GetAllMeetingByUserMeetingDto.of(
            meeting.getId(),
            meeting.getTitle(),
            meeting.getDesc(),
            meeting.getImageURL().get(0).getUrl(),
            meeting.getCategory().getValue()
        ))
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
  public User getUserById(Integer userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException(NOT_FOUND_USER.getErrorCode()));
  }

  @Override
  public UserV2GetUserOwnProfileResponseDto getUserOwnProfile(Integer userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException(NOT_FOUND_USER.getErrorCode()));
    return UserV2GetUserOwnProfileResponseDto.of(user.getId(), user.getOrgId(), user.getName(),
        user.getProfileImage(), !user.getActivities().isEmpty());
  }

  //TODO - 걍 responseDto에 담으면 됨 / status code 안넘겨줘야 됨 -> 마지막에 확ㅇ니

  @Override
  public UserV2GetCreatedMeetingByUserResponseDto getCreatedMeetingByUser(Integer userId) {
    List<Meeting> meetings = meetingRepository.findCreatedMeetingByUser(userId);
    return UserV2GetCreatedMeetingByUserResponseDto.of(meetings, meetings.size());
  }

  @Override
  public UserV2GetAppliedMeetingByUserResponseDto getAppliedMeetingByUser(Integer userId) {
    List<Apply> applies = meetingRepository.findAppliedMeetingByUser(userId);
//    { 이런식으로
//      apply : {},
//      count : {}
//    }
    // UserV2GetAppliedMeetingByUserResponseDto.of(applies, applies.size());
    return null;
  }
}
