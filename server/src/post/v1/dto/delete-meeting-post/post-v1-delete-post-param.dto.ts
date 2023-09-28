import { ApiProperty } from '@nestjs/swagger';
import { IsNotEmpty, IsNumber } from 'class-validator';

export class PostV1DeletePostParamDto {
  @ApiProperty({
    description: '게시글 ID',
    example: 1,
  })
  @IsNotEmpty()
  @IsNumber()
  postId: number;
}
