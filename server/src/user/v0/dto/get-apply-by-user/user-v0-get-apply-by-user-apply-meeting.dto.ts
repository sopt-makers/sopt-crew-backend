import { ApiProperty, OmitType } from '@nestjs/swagger';
import { Meeting } from 'src/entity/meeting/meeting.entity';
import { MeetingV0MeetingStatus } from 'src/meeting/v0/enum/meeting-v0-meeting-status.enum';

export class UserV0GetApplyByUserApplyMeetingDto extends OmitType(Meeting, [
  'hasId',
  'save',
  'remove',
  'softRemove',
  'recover',
  'reload',
] as const) {
  @ApiProperty({ enum: MeetingV0MeetingStatus })
  status: MeetingV0MeetingStatus;
}
