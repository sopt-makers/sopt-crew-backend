import { IsNotEmpty, IsNumber } from 'class-validator';
import { PageOptionsDto } from 'src/common/pagination/dto/page-options.dto';

/**
 * 댓글 목록 조회 쿼리
 * @author @rdd9223
 */
export class CommentV1GetCommentsQueryDto extends PageOptionsDto {
  /** 게시글 ID */
  @IsNotEmpty()
  @IsNumber()
  postId: number;
}
