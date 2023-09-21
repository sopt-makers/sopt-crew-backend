import { IsBoolean, IsNotEmpty } from 'class-validator';

/**
 * 댓글 좋아요 토글 응답 DTO
 */
export class CommentV1SwitchCommentLikeResponseDto {
  /** 본인이 댓글 좋아요를 눌렀는지 여부 */
  @IsNotEmpty()
  @IsBoolean()
  isLiked: boolean;
}
