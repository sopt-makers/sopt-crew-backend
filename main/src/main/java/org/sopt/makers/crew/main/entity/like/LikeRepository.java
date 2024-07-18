package org.sopt.makers.crew.main.entity.like;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, Integer> {

	List<Like> findAllByUserIdAndPostIdNotNull(Integer userId);
}
