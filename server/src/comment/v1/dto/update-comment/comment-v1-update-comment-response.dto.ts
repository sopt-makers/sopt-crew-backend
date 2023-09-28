import { PickType } from '@nestjs/swagger';
import { Comment } from 'src/entity/comment/comment.entity';

export class CommentV1UpdateCommentResponseDto extends PickType(Comment, [
  'id',
  'contents',
  'updatedDate',
]) {}
