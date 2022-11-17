import {
  IsNotEmpty,
  IsDate,
  IsString,
  IsOptional,
  IsNumber,
} from 'class-validator';

import { ApiProperty } from '@nestjs/swagger';
import { Type } from 'class-transformer';

export enum MeetingStatus {
  ALL = 0,
  BEFORE = 1,
  OPEN = 2,
  CLOSE = 3,
}

export class GetMeetingDto {
  @ApiProperty({
    example: '스터디,번개',
    description: '카테고리',
    required: false,
  })
  @IsOptional()
  readonly category: string;

  @ApiProperty({
    example: '1,2,3',
    description: '0: 전체, 1: 모집 전, 2: 모집 중, 3: 모집 마감',
    required: false,
  })
  @IsOptional()
  @IsString()
  readonly status: string;

  @ApiProperty({
    example: '스터디',
    description: '검색 내용',
    required: false,
  })
  @IsOptional()
  readonly query: string;
}
