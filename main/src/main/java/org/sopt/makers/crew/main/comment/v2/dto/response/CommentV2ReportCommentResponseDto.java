package org.sopt.makers.crew.main.comment.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
@Schema(name = "CommentV2ReportCommentResponseDto", description = "댓글 신고 응답 Dto")
public class CommentV2ReportCommentResponseDto {

  /**
   * 생성된 신고 id
   */
  @Schema(description = "신고 id", example = "1")
  private Integer reportId;
}
