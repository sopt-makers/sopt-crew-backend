import { IsNotEmpty, IsNumber } from 'class-validator';

/**
 * GetPostCount 메서드 요청 쿼리
 * @author @rdd9223
 */
export class PostV1GetPostCountQueryDto {
  /** 모임 id */
  @IsNotEmpty()
  @IsNumber()
  meetingId: number;
}
