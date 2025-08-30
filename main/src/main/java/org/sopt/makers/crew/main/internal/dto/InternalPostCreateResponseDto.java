package org.sopt.makers.crew.main.internal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "InternalPostCreateResponseDto", description = "게시글 생성 응답 Dto")
public record InternalPostCreateResponseDto(

	@Schema(description = "게시글 id", example = "1")
	@NotNull
	Integer postId
) {
	public static InternalPostCreateResponseDto from(Integer postId) {
		return new InternalPostCreateResponseDto(postId);
	}
}
