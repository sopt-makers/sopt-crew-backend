package org.sopt.makers.crew.main.entity.comment;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Integer> {

  default Comment findByIdOrThrow(Integer commentId) {
    return findById(commentId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));
  }
}
