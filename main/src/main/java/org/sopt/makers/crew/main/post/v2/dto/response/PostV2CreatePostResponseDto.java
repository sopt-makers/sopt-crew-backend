package org.sopt.makers.crew.main.post.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
@Schema(name = "PostV2CreatePostResponseDto", description = "게시글 생성 응답 Dto")
public class PostV2CreatePostResponseDto {

	/**
	 * 생성된 게시물 id
	 */
	@Schema(description = "게시글 id", example = "1")
	@NotNull
	private Integer postId;

}
