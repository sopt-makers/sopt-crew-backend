package org.sopt.makers.crew.main.entity.comment;

import org.sopt.makers.crew.main.common.exception.BadRequestException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Integer> {

  default Comment findByIdOrThrow(Integer commentId) {
    return this.findById(commentId)
        .orElseThrow(() -> new BadRequestException());
  }

  @Modifying
  @Query("update Comment c set c.likeCount = c.likeCount + 1 where c.id = :commentId")
  void incrementLikeCount(@Param("commentId") Integer commentId);

  @Modifying
  @Query("update Comment c set c.likeCount = c.likeCount - 1 where c.id = :commentId")
  void decrementLikeCount(@Param("commentId") Integer commentId);
}