package org.sopt.makers.crew.main.comment.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "댓글 수정 request body dto")
public class CommentV2UpdateCommentBodyDto {

	@Schema(example = "알고보면 쓸데있는 개발 프로세스", description = "댓글 내용")
	@NotEmpty
	private String contents;

}