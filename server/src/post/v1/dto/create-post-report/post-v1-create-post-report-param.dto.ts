import { ApiProperty } from '@nestjs/swagger';
import { IsNotEmpty, IsNumber } from 'class-validator';

/**
 * 게시글 신고 생성 body Dto
 * @author @rdd9223
 */
export class PostV1CreatePostReportParamDto {
  @ApiProperty({
    example: 1,
    description: '게시글 ID',
  })
  @IsNotEmpty()
  @IsNumber()
  postId: number;
}
