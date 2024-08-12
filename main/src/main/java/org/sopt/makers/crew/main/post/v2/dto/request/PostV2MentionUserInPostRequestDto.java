package org.sopt.makers.crew.main.post.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "모임 게시글에서 유저 언급 request body dto")
public class PostV2MentionUserInPostRequestDto {

	@Schema(example = "[111, 112, 113]", required = true, description = "언급할 유저 플그 id")
	@NotEmpty
	private final List<Integer> playgroundUserIds;

	@Schema(example = "1", required = true, description = "게시글 ID")
	@NotNull
	private final Integer postId;

	@Schema(example = "멘션내용~~", required = true, description = "멘션 내용")
	private final String content;
}
