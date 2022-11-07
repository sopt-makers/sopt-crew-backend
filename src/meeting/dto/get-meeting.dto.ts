import {
  IsNotEmpty,
  IsDate,
  IsString,
  IsOptional,
  IsNumber,
} from 'class-validator';

import { ApiProperty } from '@nestjs/swagger';
import { Type } from 'class-transformer';

export class GetMeetingDto {
  @ApiProperty({
    example: '스터디,번개',
    description: '카테고리',
    required: false,
  })
  @IsOptional()
  readonly category: string;

  @ApiProperty({
    example: 0,
    description: '0: 전체, 1: 모집중, 2: 모집마감',
    required: false,
  })
  @Type(() => Number)
  @IsOptional()
  @IsNumber()
  readonly status: number;

  @ApiProperty({
    example: '스터디',
    description: '검색 내용',
    required: false,
  })
  @IsOptional()
  readonly query: string;
}
