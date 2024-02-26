package org.sopt.makers.crew.main.comment.v2.dto.response;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 댓글 좋아요 토글 응답 DTO
 */
@Getter
@AllArgsConstructor(staticName = "of")
public class CommentV2SwitchCommentLikeResponseDto {
  /**
   * 본인이 댓글 좋아요를 눌렀는지 여부
   */
  @NotNull
  private Boolean isLiked;
}
