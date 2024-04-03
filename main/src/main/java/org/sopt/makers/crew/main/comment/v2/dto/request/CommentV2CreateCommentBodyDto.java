package org.sopt.makers.crew.main.comment.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "댓글 생성 request body dto")
public class CommentV2CreateCommentBodyDto {

  @Schema(example = "1", required = true, description = "게시글 ID")
  @NotNull
  private Integer postId;

  @Schema(example = "알고보면 쓸데있는 개발 프로세스", required = true, description = "댓글 내용")
  @NotEmpty
  private String contents;

}
