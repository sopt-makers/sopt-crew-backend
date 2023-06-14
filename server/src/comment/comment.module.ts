import { Module } from '@nestjs/common';
import { CommentV1Module } from './v1/comment-v1.module';

@Module({
  imports: [CommentV1Module],
})
export class CommentModule {}
