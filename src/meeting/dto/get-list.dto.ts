import {
  IsNotEmpty,
  IsDate,
  IsString,
  IsOptional,
  IsEnum,
} from 'class-validator';
import { Type } from 'class-transformer';

import { ApiProperty } from '@nestjs/swagger';
import { PageOptionsDto } from 'src/pagination/dto/page-options.dto';
import { ApplyStatus, ApplyType } from '../apply.entity';

export enum ListDate {
  DESC = 'desc',
  ASC = 'asc',
}

export class GetListDto extends PageOptionsDto {
  @ApiProperty({
    example: 0,
    description: '0: 대기, 1: 승인된 신청자, 2: 거절된 신청자',
    required: false,
  })
  @IsOptional()
  @Type(() => Number)
  @IsEnum(ApplyStatus)
  readonly status: ApplyStatus;

  @ApiProperty({
    example: 0,
    description: '0: 지원, 1: 초대',
    required: false,
  })
  @IsOptional()
  @Type(() => Number)
  @IsEnum(ApplyType)
  readonly type: ApplyType;

  @ApiProperty({
    example: 'desc',
    description: 'desc : 최신순, asc : 오래된 순',
    required: false,
  })
  @IsOptional()
  @IsEnum(ListDate)
  readonly date: ListDate;
}
