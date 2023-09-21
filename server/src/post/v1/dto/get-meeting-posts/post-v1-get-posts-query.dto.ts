import { IsNotEmpty, IsNumber } from 'class-validator';
import { PageOptionsDto } from 'src/common/pagination/dto/page-options.dto';

/**
 * 모임 게시글 목록 조회 쿼리
 * @author @rdd9223
 */
export class PostV1GetPostsQueryDto extends PageOptionsDto {
  /** 모임 id */
  @IsNotEmpty()
  @IsNumber()
  meetingId: number;
}
