package org.sopt.makers.crew.main.entity.like;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import jakarta.transaction.Transactional;

public interface LikeRepository extends JpaRepository<Like, Integer> {

  Optional<Like> findByUserIdAndCommentId(Integer userId, Integer commentId);

  @Modifying
  @Transactional
  Optional<Like> deleteByUserIdAndCommentId(Integer userId, Integer commentId);
}