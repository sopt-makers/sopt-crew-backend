import { ApiProperty } from '@nestjs/swagger';
import { IsNotEmpty, IsNumber } from 'class-validator';

/**
 * 댓글 삭제 param Dto
 * @author @rdd9223
 */
export class CommentV1DeleteCommentParamDto {
  @ApiProperty({
    example: 1,
    description: '댓글 ID',
  })
  @IsNotEmpty()
  @IsNumber()
  commentId: number;
}
