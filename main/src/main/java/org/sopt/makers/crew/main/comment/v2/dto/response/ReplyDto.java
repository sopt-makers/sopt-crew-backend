package org.sopt.makers.crew.main.comment.v2.dto.response;

import java.time.LocalDateTime;
import org.sopt.makers.crew.main.entity.comment.Comment;
import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(name = "ReplyDto", description = "대댓글 객체 응답 Dto")
public class ReplyDto {

	@Schema(description = "대댓글 id", example = "1")
	private final Integer id;

	@Schema(description = "댓글 내용", example = "이것은 댓글 내용입니다.")
	private final String contents;

	@Schema(description = "댓글 작성자 객체", example = "")
	private final CommentWriterDto user;

	/*
	* 변수명 createDate가 맞으나 레거시로 인해 updatedDate로 되어있음.
	* */
	@Schema(description = "댓글 생성 시점(현재 변수명은 레거시이다.)", example = "2024-07-31T15:30:00")
	private final LocalDateTime updatedDate;

	@Schema(description = "좋아요 갯수", example = "20")
	private final int likeCount;

	@Schema(description = "댓글 좋아요 여부", example = "true")
	private final Boolean isLiked;

	@Schema(description = "댓글 작성자 여부", example = "true")
	private final Boolean isWriter;

	@Schema(description = "댓글 순서", example = "2")
	private final int order;

	@QueryProjection
	public ReplyDto(Integer id, String contents, CommentWriterDto user, LocalDateTime updatedDate, int likeCount,
		Boolean isLiked, Boolean isWriter, int order) {
		this.id = id;
		this.contents = contents;
		this.user = user;
		this.updatedDate = updatedDate;
		this.likeCount = likeCount;
		this.isLiked = isLiked;
		this.isWriter = isWriter;
		this.order = order;
	}

	public static ReplyDto of(Comment comment, boolean isLiked, boolean isWriter) {
		return new ReplyDto(comment.getId(), comment.getContents(),
			new CommentWriterDto(comment.getUser().getId(), comment.getUser().getOrgId(), comment.getUser().getName(),
				comment.getUser().getProfileImage()), comment.getCreatedDate(), comment.getLikeCount(),
			isLiked, isWriter, comment.getOrder());
	}
}
