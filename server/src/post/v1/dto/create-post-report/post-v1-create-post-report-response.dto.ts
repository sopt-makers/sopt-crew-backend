import { IsNotEmpty, IsNumber } from 'class-validator';

export class PostV1CreatePostReportResponseDto {
  /** 생성된 신고 id */
  @IsNotEmpty()
  @IsNumber()
  reportId: number;
}
