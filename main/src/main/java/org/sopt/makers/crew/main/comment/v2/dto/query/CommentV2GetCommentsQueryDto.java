package org.sopt.makers.crew.main.comment.v2.dto.query;

import org.sopt.makers.crew.main.global.pagination.dto.PageOptionsDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Schema(name = "CommentV2GetCommentsQueryDto", description = "댓글 목록 요청 Dto")
public class CommentV2GetCommentsQueryDto extends PageOptionsDto {

	@Schema(description = "게시글 id", example = "1")
	@NotNull
	private final Integer postId;

	@Builder
	public CommentV2GetCommentsQueryDto(Integer page, Integer take, Integer postId) {
		super(page, take);
		this.postId = postId;
	}
}
