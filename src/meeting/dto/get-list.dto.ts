import { IsNotEmpty, IsDate, IsString, IsOptional } from 'class-validator';
import { Type } from 'class-transformer';

import { ApiProperty } from '@nestjs/swagger';

export enum ListStatus {
  ALL = 0,
  APPROVE = 1,
  REJECT = 2,
}

export enum ListDate {
  DESC = 'desc',
  ASC = 'asc',
}

export class GetListDto {
  @ApiProperty({
    example: 10,
    description: 'n명씩 보기',
    required: false,
  })
  @IsOptional()
  readonly limit: number;

  @ApiProperty({
    example: 0,
    description: '0: 전체, 1: 승인한 신청자, 2: 거절한 신청자',
    required: false,
  })
  @IsOptional()
  readonly status: ListStatus;

  @ApiProperty({
    example: 'desc',
    description: 'desc : 최신순, asc : 오래된 순',
    required: false,
  })
  @IsOptional()
  readonly date: ListDate;
}
