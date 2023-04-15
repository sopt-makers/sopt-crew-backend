import { ApiProperty } from '@nestjs/swagger';
import {
  IsString,
  IsNotEmpty,
  IsDate,
  IsNumber,
  IsOptional,
  IsBoolean,
  IsEnum,
  ArrayMaxSize,
} from 'class-validator';
import { MeetingCategory } from 'src/entity/meeting/enum/meeting-category.enum';
import { MeetingJoinablePart } from 'src/entity/meeting/enum/meeting-joinable-part.enum';

/**
 * 미팅 생성 body Dto
 * @author @rdd9223
 */
export class MeetingV1CreateMeetingBodyDto {
  @ApiProperty({
    example: '알고보면 쓸데있는 개발 프로세스',
    description: '모임 제목',
    required: true,
  })
  @IsString()
  @IsNotEmpty()
  readonly title: string;

  @ApiProperty({
    example:
      'https://makers-web-img.s3.ap-northeast-2.amazonaws.com/meeting/2023/04/12/7bd87736-b557-4b26-a0d5-9b09f1f1d7df',
    description: '모임 이미지 리스트, 최대 6개',
    required: true,
    isArray: true,
    maxLength: 6,
  })
  @IsNotEmpty()
  @ArrayMaxSize(6)
  @IsString({ each: true })
  files: string[];

  @ApiProperty({
    example: '스터디',
    description: '모임 카테고리',
    required: true,
  })
  @IsNotEmpty()
  @IsString()
  readonly category: MeetingCategory;

  @ApiProperty({
    example: '2022.10.08',
    description: '모집 기간 시작 날짜',
    required: true,
  })
  @IsNotEmpty()
  @IsDate()
  readonly startDate: Date;

  @ApiProperty({
    example: '2022.10.09',
    description: '모집 기간 끝 날짜',
    required: true,
  })
  @IsNotEmpty()
  @IsDate()
  readonly endDate: Date;

  @ApiProperty({
    example: 5,
    description: '모집 인원',
    required: true,
  })
  @IsNotEmpty()
  @IsNumber()
  readonly capacity: number;

  @ApiProperty({
    example: 'api 가 터졌다고? 깃이 터졌다고?',
    description: '모임 정보',
    required: true,
  })
  @IsNotEmpty()
  @IsString()
  readonly desc: string;

  @ApiProperty({
    example: '소요 시간 : 1시간 예상',
    description: '진행 방식 소개',
    required: true,
  })
  @IsNotEmpty()
  @IsString()
  readonly processDesc: string;

  @ApiProperty({
    example: '2022.10.29',
    description: '모임 기간 첫 날짜',
    required: true,
  })
  @IsNotEmpty()
  @IsDate()
  readonly mStartDate: Date;

  @ApiProperty({
    example: '2022.10.30',
    description: '모임 기간 날짜',
    required: true,
  })
  @IsNotEmpty()
  @IsDate()
  readonly mEndDate: Date;

  @ApiProperty({
    example: '안녕하세요 기획 파트 000입니다',
    description: '개설자 소개',
    required: true,
  })
  @IsNotEmpty()
  @IsString()
  readonly leaderDesc: string;

  @ApiProperty({
    example: '개발 모르는 사람도 환영',
    description: '모집 대상',
    required: true,
  })
  @IsNotEmpty()
  @IsString()
  readonly targetDesc: string;

  @ApiProperty({
    example: '유의할 사항',
    description: '유의 사항',
    required: false,
  })
  @IsOptional()
  @IsString()
  readonly note?: string | null;

  @ApiProperty({
    example: false,
    description: '멘토 필요 여부',
    required: true,
  })
  @IsNotEmpty()
  @IsBoolean()
  readonly isMentorNeeded: boolean;

  @ApiProperty({
    example: false,
    description: '활동기수만 지원 가능 여부',
    required: true,
  })
  @IsNotEmpty()
  @IsBoolean()
  readonly canJoinOnlyActiveGeneration: boolean;

  @ApiProperty({
    example: [MeetingJoinablePart.ANDROID, MeetingJoinablePart.IOS],
    description: '대상 파트 목록',
    enum: MeetingJoinablePart,
    isArray: true,
    required: true,
  })
  @IsEnum(MeetingJoinablePart, { each: true })
  readonly joinableParts: MeetingJoinablePart[];
}
