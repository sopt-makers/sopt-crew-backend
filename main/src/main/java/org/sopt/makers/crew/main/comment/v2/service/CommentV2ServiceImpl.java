package org.sopt.makers.crew.main.comment.v2.service;

import static org.sopt.makers.crew.main.internal.notification.PushNotificationEnums.NEW_COMMENT_PUSH_NOTIFICATION_TITLE;
import static org.sopt.makers.crew.main.internal.notification.PushNotificationEnums.PUSH_NOTIFICATION_CATEGORY;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.crew.main.comment.v2.dto.request.CommentV2CreateCommentBodyDto;
import org.sopt.makers.crew.main.comment.v2.dto.response.CommentV2CreateCommentResponseDto;
import org.sopt.makers.crew.main.entity.comment.Comment;
import org.sopt.makers.crew.main.entity.comment.CommentRepository;
import org.sopt.makers.crew.main.entity.post.Post;
import org.sopt.makers.crew.main.entity.post.PostRepository;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.internal.notification.PushNotificationService;
import org.sopt.makers.crew.main.internal.notification.dto.PushNotificationRequestDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentV2ServiceImpl implements CommentV2Service {

  private final PostRepository postRepository;
  private final UserRepository userRepository;
  private final CommentRepository commentRepository;
  private final PushNotificationService pushNotificationService;

  @Value("${push-notification.web-url}")
  private String pushWebUrl;

  /**
   * 모임 게시글 댓글 작성
   *
   * @throws 400 존재하지 않는 게시글일 떄
   * @apiNote 모임에 속한 유저만 작성 가능
   */
  @Override
  @Transactional
  public CommentV2CreateCommentResponseDto createComment(CommentV2CreateCommentBodyDto requestBody,
      Integer userId) {
    Post post = postRepository.findByIdOrThrow(requestBody.getPostId());
    User user = userRepository.findByIdOrThrow(userId);

    Comment comment = Comment.builder()
        .contents(requestBody.getContents())
        .user(user)
        .post(post)
        .build();

    Comment savedComment = commentRepository.save(comment);

    User PostWriter = post.getUser();
    String[] userIds = {String.valueOf(PostWriter.getOrgId())};

    String pushNotificationContent = String.format("[%s의 댓글] : \"%s\"",
        user.getName(), requestBody.getContents());
    String pushNotificationWeblink = pushWebUrl + "/post?id=" + post.getId();

    PushNotificationRequestDto pushRequestDto = PushNotificationRequestDto.of(userIds,
        NEW_COMMENT_PUSH_NOTIFICATION_TITLE.getValue(),
        pushNotificationContent,
        PUSH_NOTIFICATION_CATEGORY.getValue(), pushNotificationWeblink);

    pushNotificationService.sendPushNotification(pushRequestDto);

    return CommentV2CreateCommentResponseDto.of(savedComment.getId());
  }

  /**
   * 모임 게시글 댓글 삭제
   *
   * @throws 400 댓글 작성자가 아닐 때
   * @apiNote 댓글 삭제시 게시글의 댓글 수를 1 감소시킴
   */
  @Override
  @Transactional
  public void deleteComment(Integer commentId, Integer userId) {
    Comment comment = commentRepository.findByIdOrThrow(commentId);

    if (!comment.getUserId().equals(userId)) {
      throw new SecurityException("댓글 작성자만 삭제할 수 있습니다.");
    }

    Post post = comment.getPost();

    post.decreaseCommentCount();
    commentRepository.delete(comment);
  }
}
