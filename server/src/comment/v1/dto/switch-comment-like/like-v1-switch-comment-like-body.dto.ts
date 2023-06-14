import { ApiProperty } from '@nestjs/swagger';
import { IsNotEmpty, IsNumber } from 'class-validator';

/** 댓글 좋아요 토글 param DTO */
export class CommentV1SwitchCommentLikeParamDto {
  @ApiProperty({
    example: 1,
    description: '댓글 ID',
    required: true,
  })
  @IsNotEmpty()
  @IsNumber()
  commentId: number;
}
