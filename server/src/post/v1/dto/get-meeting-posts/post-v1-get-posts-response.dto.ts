import { PageMetaDto } from 'src/common/pagination/dto/page-meta.dto';
import { PostV1GetPostsResponsePostDto } from './post-v1-get-posts-response-post.dto';
import { IsInstance, ValidateNested } from 'class-validator';
import { Type } from 'class-transformer';

export class PostV1GetPostsResponseDto {
  /** 게시물 목록 */
  @IsInstance(PostV1GetPostsResponsePostDto, { each: true })
  @ValidateNested({ each: true })
  @Type(() => PostV1GetPostsResponsePostDto)
  posts: PostV1GetPostsResponsePostDto[];

  /** 페이지네이션 정보 */
  @IsInstance(PageMetaDto)
  @ValidateNested()
  @Type(() => PageMetaDto)
  meta: PageMetaDto;
}
