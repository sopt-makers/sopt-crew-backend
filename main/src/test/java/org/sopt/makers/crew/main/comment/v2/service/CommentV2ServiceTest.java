package org.sopt.makers.crew.main.comment.v2.service;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.makers.crew.main.comment.v2.dto.response.CommentV2ReportCommentResponseDto;
import org.sopt.makers.crew.main.comment.v2.dto.response.CommentV2UpdateCommentResponseDto;
import org.sopt.makers.crew.main.common.exception.BadRequestException;
import org.sopt.makers.crew.main.common.exception.ForbiddenException;
import org.sopt.makers.crew.main.entity.comment.Comment;
import org.sopt.makers.crew.main.entity.comment.CommentRepository;
import org.sopt.makers.crew.main.entity.post.Post;
import org.sopt.makers.crew.main.entity.report.Report;
import org.sopt.makers.crew.main.entity.report.ReportRepository;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserFixture;
import org.sopt.makers.crew.main.entity.user.UserRepository;

@ExtendWith(MockitoExtension.class)
public class CommentV2ServiceTest {

  @InjectMocks
  private CommentV2ServiceImpl commentV2Service;
  @Mock
  private CommentRepository commentRepository;
  @Mock
  private ReportRepository reportRepository;
  @Mock
  private UserRepository userRepository;

  private Comment comment;

  private Post post;

  private User user;

  private Report report;

  @BeforeEach
  void init() {
    user = UserFixture.createStaticUser();
    user.setUserIdForTest(1);

    String[] images = {"image1", "image2", "image3"};
    this.post = Post.builder().user(user).title("title").contents("contents").images(images)
        .build();
    this.comment = Comment.builder().contents("contents").post(post).user(user).build();
    post.addComment(this.comment);

    this.report = Report.builder().comment(comment).user(user).build();
  }

  @Nested
  class 댓글_수정 {

    @Test
    void 성공() {
      // given
      String updatedContents = "updatedContents";
      LocalDateTime expectedUpdatedDate = LocalDateTime.now();

      doReturn(comment).when(commentRepository).findByIdOrThrow(any());

      // when
      CommentV2UpdateCommentResponseDto result = commentV2Service.updateComment(comment.getId(),
          updatedContents, user.getId());

      // then
      Assertions.assertThat(result.getId()).isEqualTo(comment.getId());
      Assertions.assertThat(result.getContents()).isEqualTo(updatedContents);
      Assertions.assertThat(LocalDateTime.parse(result.getUpdateDate()))
          .isCloseTo(expectedUpdatedDate,
              Assertions.within(1, ChronoUnit.SECONDS)
          );
    }

    @Test
    void 실패_본인_작성_댓글_아님() {
      // given
      doReturn(comment).when(commentRepository).findByIdOrThrow(any());

      // when & then
      assertThrows(ForbiddenException.class, () -> {
        commentV2Service.updateComment(comment.getId(), "updatedContents",
            comment.getUser().getId() + 1);
      });
    }
  }

  @Nested
  class 댓글_삭제 {

    @Test
    void 성공() {
      // given
      int initialCommentCount = post.getCommentCount();
      doReturn(comment).when(commentRepository).findByIdOrThrow(any());

      // when
      commentV2Service.deleteComment(comment.getId(), user.getId());

      // then
      Assertions.assertThat(commentRepository.findById(comment.getId()))
          .isEqualTo(Optional.empty());
      Assertions.assertThat(post.getCommentCount()).isEqualTo(initialCommentCount - 1);
    }

    @Test
    void 실패_본인_작성_댓글_아님() {
      // given
      doReturn(comment).when(commentRepository).findByIdOrThrow(any());

      // when & then
      assertThrows(ForbiddenException.class, () -> {
        commentV2Service.deleteComment(comment.getId(), comment.getUser().getId() + 1);
      });
    }
  }

  @Nested
  class 댓글_신고 {

    @Test
    void 댓글_신고_성공() {
      // given
      doReturn(comment).when(commentRepository).findByIdOrThrow(any());
      doReturn(user).when(userRepository).findByIdOrThrow(any());
      doReturn(Optional.empty()).when(reportRepository).findByCommentAndUser(any(), any());
      doReturn(report).when(reportRepository).save(any());

      // when
      CommentV2ReportCommentResponseDto result = commentV2Service.reportComment(comment.getId(),
          user.getId());

      // then
      Assertions.assertThat(result.getReportId()).isEqualTo(report.getId());
    }

    @Test
    void 댓글_신고_실패_이미_신고한_댓글() {
      // given
      doReturn(comment).when(commentRepository).findByIdOrThrow(any());
      doReturn(user).when(userRepository).findByIdOrThrow(any());
      doReturn(Optional.of(report)).when(reportRepository).findByCommentAndUser(any(), any());

      // when & then
      assertThrows(BadRequestException.class, () -> {
        commentV2Service.reportComment(comment.getId(), user.getId());
      });
    }
  }
}
