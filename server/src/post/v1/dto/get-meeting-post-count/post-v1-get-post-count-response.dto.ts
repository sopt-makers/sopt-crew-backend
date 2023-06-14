import { IsNotEmpty, IsNumber } from 'class-validator';

/**
 * GetPostCount 메서드 응답 결과
 * @author @rdd9223
 */
export class PostV1GetPostCountResponseDto {
  /** 모임 게시글 개수 */
  @IsNotEmpty()
  @IsNumber()
  postCount: number;
}
