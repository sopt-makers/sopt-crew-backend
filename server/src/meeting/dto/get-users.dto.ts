import { IsString, IsOptional, IsNumber } from 'class-validator';
import { Type } from 'class-transformer';

import { ApiProperty } from '@nestjs/swagger';

export class GetUsersDto {
  @ApiProperty({
    example: '이동이동',
    description: '이름 검색',
    required: false,
  })
  @IsString()
  @IsOptional()
  readonly name: string;

  @ApiProperty({
    example: 30,
    description: '기수',
    required: false,
  })
  @IsNumber()
  @Type(() => Number)
  @IsOptional()
  readonly generation: number;
}
