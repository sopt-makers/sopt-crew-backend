package org.sopt.makers.crew.main.post.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
@Schema(name = "PostV2SwitchPostLikeResponseDto", description = "모임 게시글 좋아요 토글 응답 Dto")
public class PostV2SwitchPostLikeResponseDto {

	@Schema(description = "본인이 게시글 좋아요를 눌렀는지 여부", example = "true")
	@NotNull
	private final Boolean isLiked;
}
