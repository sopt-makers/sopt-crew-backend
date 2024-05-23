import { Type } from 'class-transformer';
import {
  IsArray,
  IsBoolean,
  IsDate,
  IsEnum,
  IsInstance,
  IsNotEmpty,
  IsNumber,
  IsOptional,
  IsString,
  ValidateNested,
} from 'class-validator';
import { MeetingCategory } from 'src/entity/meeting/enum/meeting-category.enum';

/**
 * 작성자 정보
 */
class PostV1GetPostsResponsePostUserDto {
  /** 작성자 고유 ID */
  @IsNotEmpty()
  @IsNumber()
  id: number;

  /** 작성자 playground 고유 ID */
  @IsNotEmpty()
  @IsNumber()
  orgId: number;

  /** 작성자 명 */
  @IsNotEmpty()
  @IsString()
  name: string;

  /** 작성자 프로필 */
  @IsOptional()
  @IsString()
  profileImage: string | null;
}

/** 게시글이 속한 모임 정보 */
class PostV1GetPostsResponsePostMeetingDto {
  @IsNotEmpty()
  @IsNumber()
  id: number;

  @IsNotEmpty()
  @IsString()
  title: string;

  @IsEnum(MeetingCategory)
  category: MeetingCategory;
}

export class PostV1GetPostsResponsePostDto {
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

  /** 게시글 게시/생성 일자 */
  @IsDate()
  createdDate: Date;

  /** 첨부 이미지 */
  @IsOptional()
  @IsNotEmpty({ each: true })
  @IsString({ each: true })
  images: string[] | null;

  /** 작성자 정보 */
  @IsInstance(PostV1GetPostsResponsePostUserDto)
  @ValidateNested()
  @Type(() => PostV1GetPostsResponsePostUserDto)
  user: PostV1GetPostsResponsePostUserDto;

  /** 좋아요 수 */
  @IsNotEmpty()
  @IsNumber()
  likeCount: number;

  /** 본인이 좋아요를 눌렀는지 여부 */
  @IsNotEmpty()
  @IsBoolean()
  isLiked: boolean;

  /** 댓글 수 */
  @IsNotEmpty()
  @IsNumber()
  commentCount: number;

  /** 댓글 작성자 썸네일 리스트 */
  @IsArray()
  @IsNotEmpty({ each: true })
  @IsString({ each: true })
  commenterThumbnails: string[];

  @IsNotEmpty()
  @IsInstance(PostV1GetPostsResponsePostMeetingDto)
  @ValidateNested()
  @Type(() => PostV1GetPostsResponsePostMeetingDto)
  meeting: PostV1GetPostsResponsePostMeetingDto;
}
