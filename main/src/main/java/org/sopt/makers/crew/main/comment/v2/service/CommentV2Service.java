package org.sopt.makers.crew.main.comment.v2.service;

import org.sopt.makers.crew.main.comment.v2.dto.request.CommentV2CreateCommentBodyDto;
import org.sopt.makers.crew.main.comment.v2.dto.response.CommentV2CreateCommentResponseDto;

public interface CommentV2Service {

  CommentV2CreateCommentResponseDto createComment(CommentV2CreateCommentBodyDto requestBody,
      Integer userId);

  void deleteComment(Integer commentId, Integer userId);
}
