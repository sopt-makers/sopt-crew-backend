import { Type } from 'class-transformer';
import {
  IsArray,
  IsBoolean,
  IsDate,
  IsInstance,
  IsNotEmpty,
  IsNumber,
  IsOptional,
  IsString,
  ValidateNested,
} from 'class-validator';

/**
 * 작성자 정보
 */
class PostV1GetPostsResponsePostUserDto {
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

  /** 게시글 게시/업데이트 일자 */
  @IsDate()
  updatedDate: Date;

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
}
