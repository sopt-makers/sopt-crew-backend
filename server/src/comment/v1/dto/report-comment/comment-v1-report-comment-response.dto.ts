import { IsNotEmpty, IsNumber } from 'class-validator';

/**
 * 댓글 신고 응답
 * @author @rdd9223
 */
export class CommentV1ReportCommentResponseDto {
  /** 생성된 신고 id */
  @IsNotEmpty()
  @IsNumber()
  reportId: number;
}
