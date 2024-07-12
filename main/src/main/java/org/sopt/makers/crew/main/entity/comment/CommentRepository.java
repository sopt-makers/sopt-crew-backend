package org.sopt.makers.crew.main.entity.comment;

import java.util.Optional;

import org.sopt.makers.crew.main.common.exception.BadRequestException;
import org.sopt.makers.crew.main.common.response.ErrorStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Integer>, CommentSearchRepository {

	default Comment findByIdOrThrow(Integer commentId) {
		return findById(commentId)
			.orElseThrow(() -> new BadRequestException(ErrorStatus.NOT_FOUND_COMMENT.getErrorCode()));
	}

	Optional<Comment> findFirstByParentIdOrderByOrderDesc(Integer parentId);

	Optional<Comment> findByIdAndPostId(Integer id, Integer postId);

	default Comment findByIdAndPostIdOrThrow(Integer id, Integer postId){
		return findByIdAndPostId(id, postId)
			.orElseThrow(() -> new BadRequestException(ErrorStatus.NOT_FOUND_COMMENT.getErrorCode()));
	}
}
