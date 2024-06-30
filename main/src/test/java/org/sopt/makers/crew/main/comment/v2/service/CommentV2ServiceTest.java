package org.sopt.makers.crew.main.comment.v2.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.makers.crew.main.common.exception.BadRequestException;
import org.sopt.makers.crew.main.entity.comment.Comment;
import org.sopt.makers.crew.main.entity.comment.CommentRepository;
import org.sopt.makers.crew.main.entity.post.Post;
import org.sopt.makers.crew.main.entity.post.PostRepository;
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
    private PostRepository postRepository;

    private Comment comment;

    private Post post;

    private User user;

    @BeforeEach
    void init() {
        user = UserFixture.createStaticUser();
        user.setUserIdForTest(1);

        String[] images = {"image1", "image2", "image3"};
        this.post = Post.builder().user(user).title("title").contents("contents").images(images).build();
        this.comment = Comment.builder().contents("contents").post(post).user(user).build();
        post.addComment(this.comment);
    }

    @Test
    void 댓글_삭제_성공() {
        // given
        int initialCommentCount = post.getCommentCount();
        doReturn(comment).when(commentRepository).findByIdOrThrow(any());

        // when
        commentV2Service.deleteComment(comment.getId(), comment.getUser().getId());

        // then
        Assertions.assertThat(commentRepository.findById(comment.getId())).isEqualTo(Optional.empty());
        Assertions.assertThat(post.getCommentCount()).isEqualTo(initialCommentCount - 1);
    }

    @Test
    void 댓글_삭제_실패_본인_작성_댓글_아님() {
        // given
        doReturn(comment).when(commentRepository).findByIdOrThrow(any());

        // when & then
        assertThrows(SecurityException.class, () -> {
            commentV2Service.deleteComment(comment.getId(), comment.getUser().getId() + 1);
        });
    }
}
