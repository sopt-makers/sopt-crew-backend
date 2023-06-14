import { ApiProperty } from '@nestjs/swagger';
import { IsNotEmpty, IsNumber } from 'class-validator';

/** 게시글 좋아요 토글 body DTO */
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
