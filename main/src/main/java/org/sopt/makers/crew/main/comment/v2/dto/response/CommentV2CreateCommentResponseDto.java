package org.sopt.makers.crew.main.comment.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
@Schema(name = "CommentV2CreateCommentResponseDto", description = "댓글 생성 응답 Dto")
public class CommentV2CreateCommentResponseDto {

  /**
   * 생성된 댓글 id
   */
  @Schema(description = "생성된 댓글 id", example = "1")
  private Integer commentId;
}
