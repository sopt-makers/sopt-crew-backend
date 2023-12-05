package org.sopt.makers.crew.main.user.v2.service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.crew.main.common.exception.BaseException;
import org.sopt.makers.crew.main.entity.apply.ApplyRepository;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyStatus;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetAllMeetingByUserMeetingDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserV2ServiceImpl implements UserV2Service {

  private final UserRepository userRepository;
  private final ApplyRepository applyRepository;

  @Override
  public List<UserV2GetAllMeetingByUserMeetingDto> getAllMeetingByUser(Integer userId) {
    User user = userRepository.findByIdOrThrow(userId);

    List<UserV2GetAllMeetingByUserMeetingDto> userJoinedList = Stream.concat(
            user.getMeetings().stream(),
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
}
