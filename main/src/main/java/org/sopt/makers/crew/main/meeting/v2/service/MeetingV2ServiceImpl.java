package org.sopt.makers.crew.main.meeting.v2.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.crew.main.common.pagination.dto.PageMetaDto;
import org.sopt.makers.crew.main.common.pagination.dto.PageOptionsDto;
import org.sopt.makers.crew.main.entity.apply.ApplyRepository;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyStatus;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.meeting.v2.dto.query.MeetingV2GetAllMeetingByOrgUserQueryDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2GetAllMeetingByOrgUserDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2GetAllMeetingByOrgUserMeetingDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingV2ServiceImpl implements MeetingV2Service {

  private final UserRepository userRepository;
  private final ApplyRepository applyRepository;

  @Override
  public MeetingV2GetAllMeetingByOrgUserDto getAllMeetingByOrgUser(
      MeetingV2GetAllMeetingByOrgUserQueryDto queryDto) {
    int page = queryDto.getPage();
    int take = queryDto.getTake();

    User user = userRepository.findByOrgIdOrThrow(queryDto.getOrgUserId());

    List<MeetingV2GetAllMeetingByOrgUserMeetingDto> userJoinedList = Stream.concat(
            user.getMeetings().stream(),
            applyRepository.findAllByUserIdAndStatus(user.getId(), EnApplyStatus.APPROVE)
                .stream()
                .map(apply -> apply.getMeeting())
        )
        .map(meeting -> MeetingV2GetAllMeetingByOrgUserMeetingDto.of(
            meeting.getId(),
            checkMeetingLeader(meeting, user.getId()),
            meeting.getTitle(),
            meeting.getImageURL().get(0).getUrl(),
            meeting.getCategory().getValue(),
            meeting.getMStartDate(),
            meeting.getMEndDate(),
            checkActivityStatus(meeting)
        ))
        .sorted(Comparator.comparing(MeetingV2GetAllMeetingByOrgUserMeetingDto::getId).reversed())
        .collect(Collectors.toList());

    List<MeetingV2GetAllMeetingByOrgUserMeetingDto> pagedUserJoinedList = userJoinedList.stream()
        .skip((long) (page - 1) * take) // 스킵할 아이템 수 계산
        .limit(take) // 페이지당 아이템 수 제한
        .collect(Collectors.toList());
    PageOptionsDto pageOptionsDto = new PageOptionsDto(page, take);
    PageMetaDto pageMetaDto = new PageMetaDto(pageOptionsDto, userJoinedList.size());
    return MeetingV2GetAllMeetingByOrgUserDto.of(pagedUserJoinedList, pageMetaDto);
  }

  private Boolean checkMeetingLeader(Meeting meeting, Integer userId) {
    return meeting.getUserId().equals(userId);
  }

  private Boolean checkActivityStatus(Meeting meeting) {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime mStartDate = meeting.getMStartDate();
    LocalDateTime mEndDate = meeting.getMEndDate();
    return now.isEqual(mStartDate) || (now.isAfter(mStartDate) && now.isBefore(mEndDate));
  }
}