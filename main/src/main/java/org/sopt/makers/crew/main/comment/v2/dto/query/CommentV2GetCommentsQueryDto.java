package org.sopt.makers.crew.main.comment.v2.dto.query;

import org.sopt.makers.crew.main.common.pagination.dto.PageOptionsDto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CommentV2GetCommentsQueryDto extends PageOptionsDto {

	@NotNull
	private final Integer postId;

	@Builder
	public CommentV2GetCommentsQueryDto(Integer page, Integer take, Integer postId) {
		super(page, take);
		this.postId = postId;
	}
}
