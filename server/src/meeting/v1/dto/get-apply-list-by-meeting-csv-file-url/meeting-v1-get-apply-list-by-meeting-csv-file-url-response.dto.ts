import { ApiProperty } from '@nestjs/swagger';
import { IsNotEmpty, IsString } from 'class-validator';

export class MeetingV1GetApplyListByMeetingCsvFileUrlResponseDto {
  @ApiProperty({
    example: 'https://example.com/apply-list.csv',
    description: 'csv 파일 url',
    required: false,
  })
  @IsNotEmpty()
  @IsString()
  url: string;
}
