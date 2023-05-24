import { Module } from '@nestjs/common';
import { NoticeV1Module } from './v1/notice-v1.module';

@Module({
  imports: [NoticeV1Module],
})
export class NoticeModule {}
