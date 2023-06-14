import { Module } from '@nestjs/common';
import { TypeOrmExModule } from 'src/db/typeorm-ex.module';
import { PostV1Service } from './post-v1.service';
import { PostV1Controller } from './post-v1.controller';
import { PostRepository } from 'src/entity/post/post.repository';

@Module({
  imports: [TypeOrmExModule.forCustomRepository([PostRepository])],
  controllers: [PostV1Controller],
  providers: [PostV1Service],
})
export class PostV1Module {}
