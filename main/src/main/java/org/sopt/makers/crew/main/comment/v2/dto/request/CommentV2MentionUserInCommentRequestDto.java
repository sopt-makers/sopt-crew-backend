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

	/**
	 * 주의!! : 필드명은 userIds 이지만 실제 요청받는 값은 orgId 입니다.
	 */

	@Schema(example = "[111, 112, 113]", required = true, description = "언급할 유저 ID, orgId 의미")
	@NotEmpty
	private List<Integer> userIds;

	@Schema(example = "1", required = true, description = "게시글 ID")
	@NotNull
	private Integer postId;

	@Schema(example = "멘션내용~~", required = true, description = "멘션 내용")
	private String content;
}
