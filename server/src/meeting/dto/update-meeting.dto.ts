import {
  IsDate,
  IsString,
  IsOptional,
  IsBoolean,
  IsEnum,
  IsNotEmpty,
} from 'class-validator';
import { Transform } from 'class-transformer';
import { ApiProperty } from '@nestjs/swagger';
import { MeetingCategory } from '../meeting.entity';
import { MeetingJoinablePart } from '../enum/meeting-joinable-part.enum';

export class UpdateMeetingDto {
  @ApiProperty({
    example: '알고보면 쓸데있는 개발 프로세스',
    description: '모임 제목',
    required: false,
  })
  @IsString()
  @IsOptional()
  readonly title?: string;

  @ApiProperty({ type: 'string', format: 'binary', required: false })
  @IsOptional()
  readonly files?: any;

  @IsString()
  @IsOptional()
  @ApiProperty({
    example: '스터디',
    description: '모임 카테고리',
    required: false,
  })
  readonly category?: MeetingCategory;

  @ApiProperty({
    example: '2022.10.08',
    description: '모집 기간 시작 날짜',
    required: false,
  })
  @IsDate()
  @IsOptional()
  readonly startDate?: Date;

  @ApiProperty({
    example: '2022.10.09',
    description: '모집 기간 끝 날짜',
    required: false,
  })
  @IsDate()
  @IsOptional()
  readonly endDate?: Date;

  @ApiProperty({
    example: 5,
    description: '모집 인원',
    required: false,
  })
  @IsOptional()
  readonly capacity?: number;

  @ApiProperty({
    example: 'api 가 터졌다고? 깃이 터졌다고?',
    description: '모임 정보',
    required: false,
  })
  @IsOptional()
  @IsString()
  readonly desc?: string;

  @ApiProperty({
    example: '소요 시간 : 1시간 예상',
    description: '진행 방식 소개',
    required: false,
  })
  @IsString()
  @IsOptional()
  readonly processDesc?: string;

  @ApiProperty({
    example: '2022.10.29',
    description: '모임 기간 첫 날짜',
    required: false,
  })
  @IsDate()
  @IsOptional()
  readonly mStartDate?: Date;

  @ApiProperty({
    example: '2022.10.30',
    description: '모임 기간 끝 날짜',
    required: false,
  })
  @IsDate()
  @IsOptional()
  readonly mEndDate?: Date;

  @ApiProperty({
    example: '안녕하세요 기획 파트 000입니다',
    description: '개설자 소개',
    required: false,
  })
  @IsString()
  @IsOptional()
  readonly leaderDesc?: string;

  @ApiProperty({
    example: '개발 모르는 사람도 환영',
    description: '모집 대상',
    required: false,
  })
  @IsString()
  @IsOptional()
  readonly targetDesc?: string;

  @ApiProperty({
    example: '유의할 사항',
    description: '유의 사항',
    required: false,
  })
  @IsString()
  @IsOptional()
  readonly note?: string;

  @ApiProperty({
    example: false,
    description: '멘토 필요 여부',
    required: false,
  })
  @IsNotEmpty()
  @IsBoolean()
  readonly isMentorNeeded?: boolean;

  @ApiProperty({
    example: false,
    description: '활동기수만 지원 가능 여부',
    required: false,
  })
  @IsNotEmpty()
  @IsBoolean()
  readonly canJoinOnlyActiveGeneration?: boolean;

  @ApiProperty({
    example: [MeetingJoinablePart.ANDROID, MeetingJoinablePart.IOS],
    description: '대상 파트 목록',
    enum: MeetingJoinablePart,
    isArray: true,
    required: false,
  })
  @Transform((property) => {
    return property.value.split(',');
  })
  @IsEnum(MeetingJoinablePart, { each: true })
  readonly joinableParts: MeetingJoinablePart[];
}
