import { IsNotEmpty, IsNumber } from 'class-validator';

export class PostV1CreatePostResponseDto {
  /** 생성된 게시물 id */
  @IsNotEmpty()
  @IsNumber()
  postId: number;
}
