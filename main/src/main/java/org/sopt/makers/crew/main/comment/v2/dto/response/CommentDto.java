package org.sopt.makers.crew.main.comment.v2.dto.response;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import lombok.Getter;

@Getter
public class CommentDto {
	private final Integer id;
	private final String contents;
	private final CommentWriterDto user;
	private final LocalDateTime updatedDate;
	private final int likeCount;
	private final boolean isLiked;
	private final boolean isWriter;
	private final int order;
	private final boolean isParentComment;
	private final Integer parentId;

	@QueryProjection
	public CommentDto(Integer id, String contents, CommentWriterDto user, LocalDateTime updatedDate, int likeCount,
		boolean isLiked, boolean isWriter, int order, boolean isParentComment, Integer parentId) {
		this.id = id;
		this.contents = contents;
		this.user = user;
		this.updatedDate = updatedDate;
		this.likeCount = likeCount;
		this.isLiked = isLiked;
		this.isWriter = isWriter;
		this.order = order;
		this.isParentComment = isParentComment;
		this.parentId = parentId;
	}
}
