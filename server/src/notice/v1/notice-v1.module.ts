import { Module } from '@nestjs/common';
import { TypeOrmExModule } from 'src/db/typeorm-ex.module';
import { NoticeRepository } from 'src/entity/notice/notice.repository';
import { NoticeV1Service } from './notice-v1.service';
import { NoticeV1Controller } from './notice-v1.controller';

@Module({
  imports: [TypeOrmExModule.forCustomRepository([NoticeRepository])],
  controllers: [NoticeV1Controller],
  providers: [NoticeV1Service],
})
export class NoticeV1Module {}
