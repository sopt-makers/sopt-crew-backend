import { ApiProperty } from '@nestjs/swagger';
import { IsDate, IsNotEmpty, IsNumber, IsString } from 'class-validator';

export class NoticeV1GetNoticesResponseDto {
  /** primary key */
  @ApiProperty({
    example: 1,
    description: 'primary key',
    required: true,
  })
  @IsNotEmpty()
  @IsNumber()
  id: number;

  /** 공지사항 제목 */
  @ApiProperty({
    example: '공지사항 제목',
    description: '공지사항 제목',
    required: true,
  })
  @IsNotEmpty()
  @IsString()
  title: string;

  /** 공지사항 부제목 */
  @ApiProperty({
    example: '공지사항 부제목',
    description: '공지사항 부제목',
    required: true,
  })
  @IsNotEmpty()
  @IsString()
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

  /** 공지사항 작성일 */
  @ApiProperty({
    example: '2021-01-01',
    description: '공지사항 작성일',
    required: true,
  })
  @IsNotEmpty()
  @IsDate()
  createdDate: Date;
}
