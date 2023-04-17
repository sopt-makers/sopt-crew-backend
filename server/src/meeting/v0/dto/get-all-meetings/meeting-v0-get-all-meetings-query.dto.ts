import { IsString, IsOptional, IsNotEmpty, IsEnum } from 'class-validator';

import { ApiProperty } from '@nestjs/swagger';
import { PageOptionsDto } from 'src/common/pagination/dto/page-options.dto';
import { MeetingJoinablePart } from '../../../../entity/meeting/enum/meeting-joinable-part.enum';
import { IsBoolean } from 'src/common/decorator/is-boolean.decorator';

export class MeetingV0GetAllMeetingsQueryDto extends PageOptionsDto {
  @ApiProperty({
    example: '스터디,번개',
    description: '카테고리',
    required: false,
  })
  @IsOptional()
  readonly category?: string;

  @ApiProperty({
    example: '0,1',
    description: '0: 모집 전, 1: 모집 중, 2: 모집 마감',
    required: false,
  })
  @IsOptional()
  @IsString()
  readonly status?: string;

  @ApiProperty({
    example: false,
    description: '활동 기수만 보기 여부',
    required: true,
  })
  @IsNotEmpty()
  @IsBoolean()
  readonly isOnlyActiveGeneration: boolean;

  @ApiProperty({
    example: Object.values(MeetingJoinablePart).join(),
    description: '검색할 활동 파트 다중 선택. OR 조건으로 검색됨',
    enum: MeetingJoinablePart,
    isArray: true,
    required: false,
  })
  @IsOptional()
  @IsEnum(MeetingJoinablePart, { each: true })
  readonly joinableParts?: MeetingJoinablePart[];

  @ApiProperty({
    example: '스터디',
    description: '검색 내용',
    required: false,
  })
  @IsOptional()
  @IsString()
  readonly query?: string;
}
