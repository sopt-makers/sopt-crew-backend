package org.sopt.makers.crew.main.post.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@Schema(name = "PostViewCountResponseDto", description = "게시글 조회수 증가용 api 응답 Dto")
@AllArgsConstructor
public class PostViewCountResponseDto {

	@Schema(description = "게시글 조회수", example = "30")
	@NotNull
	private final int viewCount;

	public static PostViewCountResponseDto of(int viewCount) {
		return new PostViewCountResponseDto(viewCount);
	}
}
