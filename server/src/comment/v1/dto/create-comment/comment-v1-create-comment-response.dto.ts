import { IsNotEmpty, IsNumber } from 'class-validator';

export class CommentV1CreateCommentResponseDto {
  /** 생성된 댓글 id */
  @IsNotEmpty()
  @IsNumber()
  commentId: number;
}
