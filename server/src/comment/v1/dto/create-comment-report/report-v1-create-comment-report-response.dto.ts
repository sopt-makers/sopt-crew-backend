import { IsNotEmpty, IsNumber } from 'class-validator';

export class CommentV1CreateCommentReportResponseDto {
  /** 생성된 신고 id */
  @IsNotEmpty()
  @IsNumber()
  reportId: number;
}
