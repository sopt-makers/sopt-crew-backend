import { ApiProperty } from '@nestjs/swagger';
import { IsDate, IsNotEmpty, IsString, Length } from 'class-validator';

export class NoticeV1CreateNoticeBodyDto {
  /** 공지사항 제목 */
  @ApiProperty({
    example: '공지사항 제목',
    description: '공지사항 제목',
    required: true,
  })
  @IsNotEmpty()
  @IsString()
  @Length(0, 20)
  title: string;

  /** 공지사항 부제목 */
  @ApiProperty({
    example: '공지사항 부제목',
    description: '공지사항 부제목',
    required: true,
  })
  @IsNotEmpty()
  @IsString()
  @Length(0, 20)
  subTitle: string;

  /** 공지사항 내용 */
  @ApiProperty({
    example: '공지사항 내용',
    description: '공지사항 내용',
    required: true,
  })
  @IsNotEmpty()
  @IsString()
  contents: string;

  /** 공지사항 노출 시작일 */
  @ApiProperty({
    example: '2021-01-01',
    description: '공지사항 노출 시작일',
    required: true,
  })
  @IsNotEmpty()
  @IsDate()
  exposeStartDate: Date;

  /** 공지사항 노출 종료일 */
  @ApiProperty({
    example: '2021-01-01',
    description: '공지사항 노출 종료일',
    required: true,
  })
  @IsNotEmpty()
  @IsDate()
  exposeEndDate: Date;
}
