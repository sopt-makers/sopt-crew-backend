package org.sopt.makers.crew.main.entity.like;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface LikeRepository extends JpaRepository<Like, Integer> {

	List<Like> findAllByUserIdAndCommentIdNotNull(Integer userId);

	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("DELETE FROM Like l WHERE l.postId = :postId")
	void deleteAllByPostId(Integer postId);

	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("DELETE FROM Like l WHERE l.postId IN :postIds")
	void deleteAllByPostIdsInQuery(List<Integer> postIds);

	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("DELETE FROM Like l WHERE l.commentId IN :commentIds")
	void deleteAllByCommentIdsInQuery(List<Integer> commentIds);

	boolean existsByUserIdAndCommentId(Integer userId, Integer commentId);

	void deleteByUserIdAndCommentId(Integer userId, Integer commentId);

	int deleteByUserIdAndPostId(Integer userId, Integer postId);

	List<Like> findAllByPostIdIsIn(List<Integer> postIds);

	List<Like> findAllByCommentIdIsIn(List<Integer> commentIds);

}
