package org.sopt.makers.crew.main.post.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class PostV2GetPostCountResponseDto {

	/**
	 * 모임 게시글 개수
	 */
	@Schema(description = "게시글 갯수", example = "25")
	@NotNull
	private Integer postCount;
}
