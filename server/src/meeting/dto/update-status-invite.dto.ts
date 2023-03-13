import { IsOptional, IsEnum } from 'class-validator';

import { ApiProperty } from '@nestjs/swagger';

export enum ApplyStatus {
  WAITING = 0,
  APPROVE = 1,
  REJECT = 2,
}

export class UpdateStatusInviteDto {
  @ApiProperty({
    example: 1,
    description: '지원 id',
    required: false,
  })
  @IsOptional()
  readonly applyId: number;

  @ApiProperty({
    example: 0,
    description: '0: 대기, 1: 승인, 2: 거절',
    required: false,
  })
  @IsEnum(ApplyStatus)
  @IsOptional()
  readonly status: ApplyStatus;
}
