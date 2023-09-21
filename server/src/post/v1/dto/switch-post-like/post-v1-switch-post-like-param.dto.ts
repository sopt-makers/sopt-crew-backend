import { ApiProperty } from '@nestjs/swagger';
import { IsNotEmpty, IsNumber } from 'class-validator';

/** 댓글 좋아요 토글 param DTO */
export class PostV1SwitchPostLikeParamDto {
  @ApiProperty({
    example: 1,
    description: '게시글 ID',
    required: true,
  })
  @IsNotEmpty()
  @IsNumber()
  postId: number;
}
