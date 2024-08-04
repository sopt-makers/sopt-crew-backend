package org.sopt.makers.crew.main.comment.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
@Schema(name = "CommentV2UpdateCommentResponseDto", description = "댓글 수정 응답 Dto")
public class CommentV2UpdateCommentResponseDto {

  /**
   * 생성된 댓글 id
   */
  @Schema(description = "수정된 댓글 id", example = "1")
  private Integer id;

  /**
   * 댓글 내용
   */
  @Schema(description = "수정된 댓글 내용", example = "댓글내용입니다1")
  private String contents;

  /**
   * 업데이트 시각
   */
  @Schema(description = "수정된 시간", example = "2024-07-31T15:30:00")
  private String updateDate;
}
