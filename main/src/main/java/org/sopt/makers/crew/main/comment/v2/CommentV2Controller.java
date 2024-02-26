package org.sopt.makers.crew.main.comment.v2;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.crew.main.common.util.UserUtil;
import org.sopt.makers.crew.main.comment.v2.dto.response.CommentV2SwitchCommentLikeResponseDto;
import org.sopt.makers.crew.main.comment.v2.service.CommentV2Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/comment/v2")
@RequiredArgsConstructor
@Tag(name = "댓글/대댓글")
public class CommentV2Controller {

  private final CommentV2Service commentV2Service;

  @Operation(summary = "댓글 좋아요 토글")
  @PostMapping("/{commentId}/like")
  @ResponseStatus(HttpStatus.OK)
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
  })
  public ResponseEntity<CommentV2SwitchCommentLikeResponseDto> switchCommentLike(
      @PathVariable Integer commentId,
      Principal principal) {
    Integer userId = UserUtil.getUserId(principal);
    CommentV2SwitchCommentLikeResponseDto commentV2SwitchCommentLikeResponseDto = this.commentV2Service
        .switchCommentLike(userId, commentId);

    return ResponseEntity.ok(commentV2SwitchCommentLikeResponseDto);
  }
}
