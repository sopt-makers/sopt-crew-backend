import { ApiProperty } from '@nestjs/swagger';
import { IsEnum, IsIn, IsNotEmpty } from 'class-validator';
import { SplitStringToNumbers } from 'src/common/decorator/split-string-to-numbers.decorator';
import { ApplyStatus } from 'src/entity/apply/enum/apply-status.enum';
import { ApplyType } from 'src/entity/apply/enum/apply-type.enum';

export class MeetingV1GetApplyListByMeetingCsvFileUrlQueryDto {
  @ApiProperty({
    example: '0,1,2',
    description: '0: 대기, 1: 승인된 신청자, 2: 거절된 신청자',
    required: true,
    type: String,
  })
  @IsNotEmpty()
  @SplitStringToNumbers()
  @IsEnum(ApplyStatus, { each: true })
  readonly status: ApplyStatus[];

  @ApiProperty({
    example: '0,1',
    description: '0: 지원, 1: 초대',
    required: true,
    type: String,
  })
  @IsNotEmpty()
  @SplitStringToNumbers()
  @IsEnum(ApplyType, { each: true })
  readonly type: ApplyType[];

  @ApiProperty({
    example: 'desc',
    description: 'desc : 최신순, asc : 오래된 순',
    required: false,
    enum: ['desc', 'asc'],
  })
  @IsIn(['desc', 'asc'])
  readonly order: 'desc' | 'asc';
}
