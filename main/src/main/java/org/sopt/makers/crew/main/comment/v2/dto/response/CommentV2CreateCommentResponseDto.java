package org.sopt.makers.crew.main.comment.v2.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class CommentV2CreateCommentResponseDto {

  /**
   * 생성된 댓글 id
   */
  private Integer commentId;
}
