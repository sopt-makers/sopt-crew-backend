import { Module } from '@nestjs/common';
import { MeetingV1Controller } from './meeting-v1.controller';
import { MeetingV1Service } from './meeting-v1.service';
import { MeetingRepository } from '../../entity/meeting/meeting.repository';
import { TypeOrmExModule } from 'src/db/typeorm-ex.module';
import { S3Module } from 'src/shared/s3/s3.module';

@Module({
  imports: [TypeOrmExModule.forCustomRepository([MeetingRepository]), S3Module],
  controllers: [MeetingV1Controller],
  providers: [MeetingV1Service],
})
export class MeetingV1Module {}
