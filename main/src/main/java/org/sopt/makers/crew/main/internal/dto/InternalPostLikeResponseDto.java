package org.sopt.makers.crew.main.internal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record InternalPostLikeResponseDto(
	@Schema(description = "본인이 게시글 좋아요를 눌렀는지 여부", example = "true")
	@NotNull
	Boolean isLiked
) {
	public static InternalPostLikeResponseDto from(Boolean isLiked) {
		return new InternalPostLikeResponseDto(isLiked);
	}
}
