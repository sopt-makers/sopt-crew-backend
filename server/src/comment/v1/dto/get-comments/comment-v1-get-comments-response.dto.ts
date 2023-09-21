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
import { PageMetaDto } from 'src/common/pagination/dto/page-meta.dto';

/**
 * 작성자 정보
 */
class CommentV1GetCommentsResponseCommentUserDto {
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
 * 댓글 정보
 */
class CommentV1GetCommentsResponseCommentDto {
  /** 댓글 고유 ID */
  @IsNotEmpty()
  @IsNumber()
  id: number;

  /** 댓글 내용 */
  @IsNotEmpty()
  @IsString()
  contents: string;

  /** 댓글 작성자 정보 */
  @IsInstance(CommentV1GetCommentsResponseCommentUserDto)
  @ValidateNested()
  @Type(() => CommentV1GetCommentsResponseCommentUserDto)
  user: CommentV1GetCommentsResponseCommentUserDto;

  /** 댓글 작성/수정 일자 */
  @IsDate()
  updatedDate: Date;

  /** 댓글 좋아요 수 */
  @IsNotEmpty()
  @IsNumber()
  likeCount: number;

  /** 본인이 댓글 좋아요를 눌렀는지 여부 */
  @IsNotEmpty()
  @IsBoolean()
  isLiked: boolean;
}

/**
 * 댓글 목록 조회 응답 DTO
 */
export class CommentV1GetCommentsResponseDto {
  /** 댓글 목록 */
  @IsInstance(CommentV1GetCommentsResponseCommentDto, { each: true })
  @ValidateNested({ each: true })
  @Type(() => CommentV1GetCommentsResponseCommentDto)
  comments: CommentV1GetCommentsResponseCommentDto[];

  /** 페이지네이션 정보 */
  @IsInstance(PageMetaDto)
  @ValidateNested()
  @Type(() => PageMetaDto)
  meta: PageMetaDto;
}
