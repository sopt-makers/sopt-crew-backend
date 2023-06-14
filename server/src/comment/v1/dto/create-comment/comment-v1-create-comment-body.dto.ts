import { ApiProperty } from '@nestjs/swagger';
import { IsString, IsNotEmpty, IsNumber } from 'class-validator';

/**
 * 댓글 생성 body Dto
 * @author @rdd9223
 */
export class CommentV1CreateCommentBodyDto {
  @ApiProperty({
    example: 1,
    description: '게시글 ID',
    required: true,
  })
  @IsNotEmpty()
  @IsNumber()
  postId: number;

  @ApiProperty({
    example: '알고보면 쓸데있는 개발 프로세스',
    description: '댓글 내용',
    required: true,
  })
  @IsNotEmpty()
  @IsString()
  contents: string;
}
