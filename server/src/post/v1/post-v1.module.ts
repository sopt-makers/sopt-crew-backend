import { Module } from '@nestjs/common';
import { TypeOrmExModule } from 'src/db/typeorm-ex.module';
import { PostV1Service } from './post-v1.service';
import { PostV1Controller } from './post-v1.controller';
import { PostRepository } from 'src/entity/post/post.repository';
import { MeetingRepository } from 'src/entity/meeting/meeting.repository';
import { LikeRepository } from 'src/entity/like/like.repository';
import { ReportRepository } from 'src/entity/report/report.repository';

@Module({
  imports: [
    TypeOrmExModule.forCustomRepository([
      PostRepository,
      MeetingRepository,
      LikeRepository,
      ReportRepository,
    ]),
  ],
  controllers: [PostV1Controller],
  providers: [PostV1Service],
})
export class PostV1Module {}
