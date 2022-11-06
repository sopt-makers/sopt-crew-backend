import { IsNotEmpty, IsDate, IsString, IsOptional } from 'class-validator';
import { Type } from 'class-transformer';

import { ApiProperty } from '@nestjs/swagger';

export class FilterMeetingDto {
  @ApiProperty({
    example: '["스터디","번개"]',
    description: '카테고리',
    required: false,
  })
  @IsOptional()
  readonly category: string[];

  @ApiProperty({
    example: 0,
    description: '0: 전체, 1: 모집중, 2: 모집마감',
    required: false,
  })
  @IsOptional()
  readonly status: number;

  @ApiProperty({
    example: '스터디',
    description: '검색 내용',
    required: false,
  })
  @IsOptional()
  readonly query: number;
}
