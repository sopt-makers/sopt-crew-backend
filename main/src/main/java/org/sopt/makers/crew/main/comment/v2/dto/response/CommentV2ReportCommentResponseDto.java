package org.sopt.makers.crew.main.comment.v2.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class CommentV2ReportCommentResponseDto {

  /**
   * 생성된 신고 id
   */
  private Integer reportId;
}
