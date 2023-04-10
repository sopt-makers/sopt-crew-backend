import { Module } from '@nestjs/common';
import { MeetingV0Module } from './v0/meeting-v0.module';

@Module({
  imports: [MeetingV0Module],
})
export class MeetingModule {}
