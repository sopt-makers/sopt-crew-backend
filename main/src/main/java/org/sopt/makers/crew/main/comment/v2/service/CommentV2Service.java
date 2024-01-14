package org.sopt.makers.crew.main.comment.v2.service;

import org.sopt.makers.crew.main.comment.v2.dto.response.CommentV2SwitchCommentLikeResponseDto;

public interface CommentV2Service {

  /**
   * 댓글 좋아요 토글
   * 
   * - 회원만 할 수 있음
   * - 좋아요 버튼 누르면 삭제했다가 다시 생김
   * 
   * @param userId    사용자 ID
   * @param commentId 댓글 ID
   * @return 댓글 좋아요 토글 응답 DTO
   */
  CommentV2SwitchCommentLikeResponseDto switchCommentLike(Integer userId, Integer commentId);

}
