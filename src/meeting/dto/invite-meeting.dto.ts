import {
  IsNotEmpty,
  IsDate,
  IsString,
  IsOptional,
  IsNumber,
  IsArray,
} from 'class-validator';

import { ApiProperty } from '@nestjs/swagger';

export class InviteMeetingDto {
  @ApiProperty({
    example: 4,
    description: '모임 id',
    required: false,
  })
  @IsNumber()
  readonly id: number;

  @ApiProperty({
    example: '안녕하세요 스터디 같이 하실래요?',
    description: '초대 메세지',
    required: false,
  })
  @IsString()
  readonly message: string;

  @ApiProperty({
    example: [1, 2, 5, 4],
    description: '모임 id',
    required: false,
  })
  // @()
  @IsArray()
  readonly userIdArr: number[];
}
