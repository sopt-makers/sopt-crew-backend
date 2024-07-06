package org.sopt.makers.crew.main.comment.v2.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class CommentV2UpdateCommentResponseDto {

  /**
   * 생성된 댓글 id
   */
  private Integer id;

  /**
   * 댓글 내용
   */
  private String contents;

  /**
   * 업데이트 시각
   */
  private String updateDate;
}
