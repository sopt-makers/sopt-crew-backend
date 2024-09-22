package org.sopt.makers.crew.main.comment.v2.dto.response;

import java.time.LocalDateTime;
import org.sopt.makers.crew.main.entity.comment.Comment;
import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@Schema(name = "ReplyDto", description = "대댓글 객체 응답 Dto")
public class ReplyDto {

	@Schema(description = "대댓글 id", example = "1")
	@NotNull
	private final Integer id;

	@Schema(description = "댓글 내용", example = "이것은 댓글 내용입니다.")
	@NotNull
	private final String contents;

	@Schema(description = "댓글 작성자 객체", example = "")
	@NotNull
	private final CommentWriterDto user;

	@Schema(description = "댓글 생성 시점", example = "2024-07-31T15:30:00")
	@NotNull
	private final LocalDateTime createdDate;

	@Schema(description = "좋아요 갯수", example = "20")
	@NotNull
	private final int likeCount;

	@Schema(description = "댓글 좋아요 여부", example = "true")
	@NotNull
	private final Boolean isLiked;

	@Schema(description = "댓글 작성자 여부", example = "true")
	@NotNull
	private final Boolean isWriter;

	@Schema(description = "댓글 순서", example = "2")
	@NotNull
	private final int order;

	@Schema(description = "차단여부", example = "false")
	@NotNull
	private final Boolean isBlockedComment;

	@QueryProjection
	public ReplyDto(Integer id, String contents, CommentWriterDto user, LocalDateTime createdDate, int likeCount,
		Boolean isLiked, Boolean isWriter, int order, Boolean isBlockedComment) {
		this.id = id;
		this.contents = contents;
		this.user = user;
		this.createdDate = createdDate;
		this.likeCount = likeCount;
		this.isLiked = isLiked;
		this.isWriter = isWriter;
		this.order = order;
		this.isBlockedComment = isBlockedComment;
	}

	public static ReplyDto of(Comment comment, boolean isLiked, boolean isWriter,  boolean isBlockedComment) {
		return new ReplyDto(comment.getId(), comment.getContents(),
			new CommentWriterDto(comment.getUser().getId(), comment.getUser().getOrgId(), comment.getUser().getName(),
				comment.getUser().getProfileImage()), comment.getCreatedDate(), comment.getLikeCount(),
			isLiked, isWriter, comment.getOrder(), isBlockedComment);
	}
}
