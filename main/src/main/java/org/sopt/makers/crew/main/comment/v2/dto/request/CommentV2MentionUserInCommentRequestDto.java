package org.sopt.makers.crew.main.comment.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "댓글에서 유저 언급 request body dto")
public class CommentV2MentionUserInCommentRequestDto {

	@Schema(example = "[111, 112, 113]", description = "메이커스 프로덕트에서 범용적으로 사용하는 userId")
	@NotEmpty
	private List<Integer> orgIds;

	@Schema(example = "1", description = "게시글 ID")
	@NotNull
	private Integer postId;

	@Schema(example = "멘션내용~~", description = "멘션 내용")
	@NotNull
	private String content;
}
