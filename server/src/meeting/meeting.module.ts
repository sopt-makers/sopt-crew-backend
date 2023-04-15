import { Module } from '@nestjs/common';
import { MeetingV0Module } from './v0/meeting-v0.module';
import { MeetingV1Module } from './v1/meeting-v1.module';

@Module({
  imports: [MeetingV0Module, MeetingV1Module],
})
export class MeetingModule {}
