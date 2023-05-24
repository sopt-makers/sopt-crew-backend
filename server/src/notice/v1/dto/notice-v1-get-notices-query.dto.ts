import { IsEnum } from 'class-validator';
import { EnGetNoticesStatus } from '../enum/get-notices-status.enum';
import { ApiProperty } from '@nestjs/swagger';

export class NoticeV1GetNoticesQueryDto {
  @ApiProperty({
    example: EnGetNoticesStatus.EXPOSING,
    description: '공지사항 상태',
    required: true,
  })
  @IsEnum(EnGetNoticesStatus)
  status: EnGetNoticesStatus;
}
