package org.sopt.makers.crew.main.entity.comment;

import java.util.List;
import java.util.Optional;

import org.sopt.makers.crew.main.common.exception.BadRequestException;
import org.sopt.makers.crew.main.common.exception.ErrorStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface CommentRepository extends JpaRepository<Comment, Integer>, CommentSearchRepository {

	default Comment findByIdOrThrow(Integer commentId) {
		return findById(commentId)
			.orElseThrow(() -> new BadRequestException(ErrorStatus.NOT_FOUND_COMMENT.getErrorCode()));
	}

	Optional<Comment> findFirstByParentIdOrderByOrderDesc(Integer parentId);

	List<Comment> findAllByParentIdOrderByOrderDesc(Integer parentId);

	Optional<Comment> findByIdAndPostId(Integer id, Integer postId);

	default Comment findByIdAndPostIdOrThrow(Integer id, Integer postId) {
		return findByIdAndPostId(id, postId)
			.orElseThrow(() -> new BadRequestException(ErrorStatus.NOT_FOUND_COMMENT.getErrorCode()));
	}

	List<Comment> findAllByPostIdOrderByCreatedDate(Integer postId);

	List<Comment> findAllByPostIdIsIn(List<Integer> postIds);

	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("DELETE FROM Comment c WHERE c.postId = :postId")
	void deleteAllByPostId(Integer postId);

	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("DELETE FROM Comment c WHERE c.postId IN :postIds")
	void deleteAllByPostIdsInQuery(List<Integer> postIds);
}
