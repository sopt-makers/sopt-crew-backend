package org.sopt.makers.crew.main.post.v2.service;

import static java.util.stream.Collectors.toList;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.crew.main.common.exception.ForbiddenException;
import org.sopt.makers.crew.main.entity.apply.ApplyRepository;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyStatus;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;
import org.sopt.makers.crew.main.entity.post.Post;
import org.sopt.makers.crew.main.entity.post.PostRepository;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.internal.notification.PushNotificationService;
import org.sopt.makers.crew.main.internal.notification.dto.PushNotificationRequestDto;
import org.sopt.makers.crew.main.post.v2.dto.request.PostV2CreatePostBodyDto;
import org.sopt.makers.crew.main.post.v2.dto.response.PostV2CreatePostResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostV2ServiceImpl implements PostV2Service {

  private final MeetingRepository meetingRepository;
  private final UserRepository userRepository;
  private final PostRepository postRepository;
  private final ApplyRepository applyRepository;
  private final PushNotificationService pushNotificationService;

  @Value("${push-notification.web-url}")
  private String pushWebUrl;

  private static final String NEW_POST_PUSH_NOTIFICATION_TITLE = "✏️내 모임에 새로운 글이 업로드됐어요.";
  private static final String PUSH_NOTIFICATION_CATEGORY = "NEWS";

  /**
   * 모임 게시글 작성
   *
   * @throws 403 모임에 속한 유저가 아닌 경우
   * @apiNote 모임에 속한 유저만 작성 가능
   */
  @Override
  @Transactional
  public PostV2CreatePostResponseDto createPost(PostV2CreatePostBodyDto requestBody,
      Integer userId) {
    Meeting meeting = meetingRepository.findByIdOrThrow(requestBody.getMeetingId());
    User user = userRepository.findByIdOrThrow(userId);

    boolean isInMeeting = meeting.getAppliedInfo().stream()
        .anyMatch(apply -> apply.getUserId().equals(userId)
            && apply.getStatus() == EnApplyStatus.APPROVE);

    boolean isMeetingCreator = meeting.getUserId().equals(userId);

    if (isInMeeting == false && isMeetingCreator == false) {
      throw new ForbiddenException("권한이 없습니다.");
    }

    Post post = Post.builder()
        .title(requestBody.getTitle())
        .user(user)
        .contents(requestBody.getContents())
        .images(requestBody.getImages())
        .meeting(meeting)
        .build();

    Post savedPost = postRepository.save(post);

    List<String> userIdList = applyRepository.findAllByMeetingIdAndStatus(meeting.getId(),
            EnApplyStatus.APPROVE)
        .stream()
        .map(apply -> String.valueOf(apply.getUser().getOrgId()))
        .collect(toList());

    String[] userIds = userIdList.toArray(new String[0]);
    String pushNotificationContent = String.format("[%s의 새 글] : \"%s\"",
        user.getName(), post.getTitle());
    String pushNotificationWeblink = pushWebUrl + "/detail?id=" + meeting.getId();

    PushNotificationRequestDto pushRequestDto = PushNotificationRequestDto.of(userIds,
        NEW_POST_PUSH_NOTIFICATION_TITLE,
        pushNotificationContent,
        PUSH_NOTIFICATION_CATEGORY, pushNotificationWeblink);

    pushNotificationService.sendPushNotification(pushRequestDto);

    return PostV2CreatePostResponseDto.of(savedPost.getId());
  }
}
