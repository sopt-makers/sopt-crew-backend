import { Module } from '@nestjs/common';
import { PostV1Module } from './v1/post-v1.module';

@Module({
  imports: [PostV1Module],
})
export class PostModule {}
