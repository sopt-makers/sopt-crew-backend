package org.sopt.makers.crew.main.comment.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
@Schema(name = "CommentV2SwitchCommentLikeResponseDto", description = "댓글 좋아요 토글 응답 Dto")
public class CommentV2SwitchCommentLikeResponseDto {

  /**
   * 요청 후 내가 좋아요를 누른 상태
   */
  @Schema(description = "요청 후 내가 좋아요를 누른 상태", example = "false")
  private Boolean isLiked;
}
