package org.sopt.makers.crew.main.entity.comment;

import org.sopt.makers.crew.main.common.exception.BadRequestException;
import org.sopt.makers.crew.main.common.response.ErrorStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Integer> {

  default Comment findByIdOrThrow(Integer commentId) {
    return findById(commentId)
        .orElseThrow(() -> new BadRequestException(ErrorStatus.NOT_FOUND_COMMENT.getErrorCode()));
  }
}
