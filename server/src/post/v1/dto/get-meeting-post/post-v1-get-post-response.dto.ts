import { Type } from 'class-transformer';
import {
  IsBoolean,
  IsDate,
  IsInstance,
  IsNotEmpty,
  IsNumber,
  IsOptional,
  IsString,
  ValidateNested,
} from 'class-validator';
import { ImageURL } from 'src/entity/meeting/interface/image-url.interface';

/**
 * 작성자 정보
 */
class PostV1GetPostResponseUserDto {
  /** 작성자 고유 ID */
  @IsNotEmpty()
  @IsNumber()
  id: number;

  /** 작성자 명 */
  @IsNotEmpty()
  @IsString()
  name: string;

  /** 작성자 프로필 */
  @IsOptional()
  @IsString()
  profileImage: string | null;
}

/**
 * 모임 정보
 */
class PostV1GetPostResponseMeetingDto {
  /** 모임 고유 ID */
  @IsNotEmpty()
  @IsNumber()
  id: number;

  /** 모임 제목 */
  @IsNotEmpty()
  @IsString()
  title: string;

  /** 모임 이미지 */
  @IsNotEmpty()
  @IsString()
  imageURL: ImageURL[];

  /** 모임 카테고리 */
  @IsNotEmpty()
  @IsString()
  category: string;
}

export class PostV1GetPostResponseDto {
  /** 게시글 고유 ID */
  @IsNotEmpty()
  @IsNumber()
  id: number;

  /** 게시글 제목 */
  @IsNotEmpty()
  @IsString()
  title: string;

  /** 게시글 내용 */
  @IsNotEmpty()
  @IsString()
  contents: string;

  /** 게시글 게시/업데이트 일자 */
  @IsDate()
  updatedDate: Date;

  /** 첨부 이미지 */
  @IsOptional()
  @IsNotEmpty({ each: true })
  @IsString({ each: true })
  images: string[] | null;

  /** 작성자 정보 */
  @IsInstance(PostV1GetPostResponseUserDto)
  @ValidateNested()
  @Type(() => PostV1GetPostResponseUserDto)
  user: PostV1GetPostResponseUserDto;

  /** 미팅 정보 */
  @IsInstance(PostV1GetPostResponseMeetingDto)
  @ValidateNested()
  @Type(() => PostV1GetPostResponseMeetingDto)
  meeting: PostV1GetPostResponseMeetingDto;

  /** 조회수 */
  @IsNotEmpty()
  @IsNumber()
  viewCount: number;

  /** 좋아요 수 */
  @IsNotEmpty()
  @IsNumber()
  likeCount: number;

  /** 본인이 좋아요를 눌렀는지 여부 */
  @IsNotEmpty()
  @IsBoolean()
  isLiked: boolean;
}
