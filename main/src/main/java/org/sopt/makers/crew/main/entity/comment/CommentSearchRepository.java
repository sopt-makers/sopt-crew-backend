package org.sopt.makers.crew.main.entity.comment;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentSearchRepository {
	Page<Comment> findAllByPostIdPagination(Integer postId, int depth, Pageable pageable);
}
