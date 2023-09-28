import { ApiProperty } from '@nestjs/swagger';
import { IsString, IsNotEmpty } from 'class-validator';

/**
 * 댓글 수정 body Dto
 * @author @rdd9223
 */
export class CommentV1UpdateCommentBodyDto {
  @ApiProperty({
    example: '알고보면 쓸데있는 개발 프로세스',
    description: '댓글 내용',
    required: true,
  })
  @IsNotEmpty()
  @IsString()
  contents: string;
}
