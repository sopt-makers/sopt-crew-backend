package org.sopt.makers.crew.main.entity.comment;

import static org.sopt.makers.crew.main.entity.comment.QComment.*;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CommentSearchRepositoryImpl implements CommentSearchRepository {
	private final JPAQueryFactory queryFactory;

	@Override
	public Page<Comment> findAllByPostIdPagination(Integer postId, int depth, Pageable pageable) {
		QComment parentComment = new QComment("parentComment");
		QComment childComment = new QComment("childComment");

		List<Comment> content = queryFactory
			.selectFrom(parentComment)
			.leftJoin(childComment).on(childComment.parentId.eq(parentComment.id))
			.where(parentComment.postId.eq(postId))
			.orderBy(parentComment.createdDate.asc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		JPAQuery<Long> countQuery = getCount(postId);

		return PageableExecutionUtils.getPage(content, PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()), countQuery::fetchFirst);
	}

	private JPAQuery<Long> getCount(Integer postId) {
		return queryFactory
			.select(comment.count())
			.from(comment)
			.where(comment.postId.eq(postId));

	}
}
