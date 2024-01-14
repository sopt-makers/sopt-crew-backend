package org.sopt.makers.crew.main.comment.v2.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;

import org.sopt.makers.crew.main.entity.comment.Comment;
import org.sopt.makers.crew.main.entity.comment.CommentRepository;
import org.sopt.makers.crew.main.entity.like.Like;
import org.sopt.makers.crew.main.entity.like.LikeRepository;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.comment.v2.dto.response.CommentV2SwitchCommentLikeResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentV2ServiceImpl implements CommentV2Service {

  private final LikeRepository likeRepository;
  private final CommentRepository commentRepository;
  private final UserRepository userRepository;

  @Override
  @Transactional
  public CommentV2SwitchCommentLikeResponseDto switchCommentLike(Integer userId, Integer commentId) {
    System.out.println(1);
    Optional<Like> like = this.likeRepository.findByUserIdAndCommentId(userId, commentId);
    System.out.println(2);
    if (!like.isPresent()) {
      User user = this.userRepository.findByIdOrThrow(userId);
      Comment comment = this.commentRepository.findByIdOrThrow(commentId);

      Like newLike = Like.builder()
          .user(user)
          .comment(comment)
          .post(null)
          .build();

      this.likeRepository.save(newLike);
      this.commentRepository.incrementLikeCount(commentId);

      return CommentV2SwitchCommentLikeResponseDto.of(true);
    }

    this.likeRepository.deleteByUserIdAndCommentId(userId, commentId);
    this.commentRepository.decrementLikeCount(commentId);

    return CommentV2SwitchCommentLikeResponseDto.of(false);
  }
}
