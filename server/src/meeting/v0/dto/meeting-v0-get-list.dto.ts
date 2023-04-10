import { IsOptional, IsEnum } from 'class-validator';

import { ApiProperty } from '@nestjs/swagger';
import { PageOptionsDto } from 'src/common/pagination/dto/page-options.dto';
import { MeetingV0ListDate } from '../enum/meeting-v0-list-date.enum';

export class MeetingV0GetListDto extends PageOptionsDto {
  @ApiProperty({
    example: 0,
    description: '0: 대기, 1: 승인된 신청자, 2: 거절된 신청자',
    required: false,
  })
  @IsOptional()
  // @Type(() => Number)
  // @IsEnum(ApplyStatus)
  readonly status: string;

  @ApiProperty({
    example: 0,
    description: '0: 지원, 1: 초대',
    required: false,
  })
  @IsOptional()
  // @IsEnum(ApplyType)
  readonly type: string;

  @ApiProperty({
    example: 'desc',
    description: 'desc : 최신순, asc : 오래된 순',
    required: false,
  })
  @IsOptional()
  @IsEnum(MeetingV0ListDate)
  readonly date: MeetingV0ListDate;
}
