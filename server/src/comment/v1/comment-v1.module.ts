import { Module } from '@nestjs/common';
import { TypeOrmExModule } from 'src/db/typeorm-ex.module';
import { CommentV1Service } from './comment-v1.service';
import { CommentV1Controller } from './comment-v1.controller';
import { CommentRepository } from 'src/entity/comment/comment.repository';
import { PostRepository } from 'src/entity/post/post.repository';
import { LikeRepository } from 'src/entity/like/like.repository';
import { ReportRepository } from 'src/entity/report/report.repository';

@Module({
  imports: [
    TypeOrmExModule.forCustomRepository([
      CommentRepository,
      PostRepository,
      LikeRepository,
      ReportRepository,
    ]),
  ],
  controllers: [CommentV1Controller],
  providers: [CommentV1Service],
})
export class CommentV1Module {}
