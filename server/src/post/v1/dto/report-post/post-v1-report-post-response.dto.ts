import { IsNotEmpty, IsNumber } from 'class-validator';

export class PostV1ReportPostResponseDto {
  /** 생성된 신고 id */
  @IsNotEmpty()
  @IsNumber()
  reportId: number;
}
