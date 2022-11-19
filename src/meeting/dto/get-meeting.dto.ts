import {
  IsNotEmpty,
  IsDate,
  IsString,
  IsOptional,
  IsNumber,
} from 'class-validator';

import { ApiProperty } from '@nestjs/swagger';
import { Type } from 'class-transformer';
import { PageOptionsDto } from 'src/pagination/dto/page-options.dto';

export enum MeetingStatus {
  ALL = 0,
  BEFORE = 1,
  OPEN = 2,
  CLOSE = 3,
}

export class GetMeetingDto extends PageOptionsDto {
  @ApiProperty({
    example: '스터디,번개',
    description: '카테고리',
    required: false,
  })
  @IsOptional()
  readonly category?: string;

  @ApiProperty({
    example: '0,1',
    description: '0: 모집 전, 1: 모집 중, 2: 모집 마감',
    required: false,
  })
  @IsOptional()
  @IsString()
  readonly status?: string;

  @ApiProperty({
    example: '스터디',
    description: '검색 내용',
    required: false,
  })
  @IsOptional()
  readonly query?: string;
}
