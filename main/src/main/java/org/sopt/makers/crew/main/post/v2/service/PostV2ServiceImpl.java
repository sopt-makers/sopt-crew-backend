package org.sopt.makers.crew.main.post.v2.service;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.crew.main.common.exception.ForbiddenException;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyStatus;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;
import org.sopt.makers.crew.main.entity.post.Post;
import org.sopt.makers.crew.main.entity.post.PostRepository;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.post.v2.dto.request.PostV2CreatePostBodyDto;
import org.sopt.makers.crew.main.post.v2.dto.response.PostV2CreatePostResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostV2ServiceImpl implements PostV2Service {

  private final MeetingRepository meetingRepository;
  private final UserRepository userRepository;
  private final PostRepository postRepository;

  /**
   * 모임 게시글 작성 - 모임에 속한 유저만 작성 가능 - 모임에 속한 유저가 아니면 403 에러
   *
   * @param userId      유저 정보
   * @param requestBody 게시글 생성 body
   * @returns PostV1CreatePostResponseDto 게시글 생성 response dto
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

    return PostV2CreatePostResponseDto.of(savedPost.getId());
  }
}