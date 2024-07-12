package org.sopt.makers.crew.main.comment.v2.dto.response;

import java.time.LocalDateTime;
import java.util.List;

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
	private final List<CommentDto> replies;

	@QueryProjection
	public CommentDto(Integer id, String contents, CommentWriterDto user, LocalDateTime updatedDate, int likeCount,
		boolean isLiked, boolean isWriter, List<CommentDto> replies) {
		this.id = id;
		this.contents = contents;
		this.user = user;
		this.updatedDate = updatedDate;
		this.likeCount = likeCount;
		this.isLiked = isLiked;
		this.isWriter = isWriter;
		this.replies = replies;
	}
}
