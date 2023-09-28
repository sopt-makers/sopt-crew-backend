import { OmitType } from '@nestjs/swagger';
import { UserV0GetApplyByUserApplyMeetingDto } from './user-v0-get-apply-by-user-apply-meeting.dto';
import { Apply } from 'src/entity/apply/apply.entity';

export class UserV0GetApplyByUserApplyDto extends OmitType(Apply, [
  'hasId',
  'save',
  'remove',
  'softRemove',
  'recover',
  'reload',
  'meeting',
] as const) {
  meeting: UserV0GetApplyByUserApplyMeetingDto;
}
