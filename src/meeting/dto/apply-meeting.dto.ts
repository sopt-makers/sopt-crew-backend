import {
  IsNotEmpty,
  IsDate,
  IsString,
  IsOptional,
  IsNumber,
} from 'class-validator';

import { ApiProperty } from '@nestjs/swagger';

export class ApplyMeetingDto {
  @ApiProperty({
    example: '4',
    description: '모임 id',
    required: false,
  })
  @IsNumber()
  readonly id: number;

  @ApiProperty({
    example: '꼭 지원하고 싶습니다',
    description: '지원 각오',
    required: false,
  })
  @IsString()
  readonly content: string;
}
