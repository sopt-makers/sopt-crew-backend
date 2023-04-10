import { IsOptional, IsEnum } from 'class-validator';

import { ApiProperty } from '@nestjs/swagger';
import { ApplyStatus } from 'src/entity/apply/enum/apply-status.enum';

export class MeetingV0UpdateStatusApplyDto {
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
