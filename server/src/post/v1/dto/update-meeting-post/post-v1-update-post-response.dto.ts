import { PickType } from '@nestjs/swagger';
import { Post } from 'src/entity/post/post.entity';

export class PostV1UpdatePostResponseDto extends PickType(Post, [
  'id',
  'title',
  'contents',
  'updatedDate',
  'images',
]) {}
