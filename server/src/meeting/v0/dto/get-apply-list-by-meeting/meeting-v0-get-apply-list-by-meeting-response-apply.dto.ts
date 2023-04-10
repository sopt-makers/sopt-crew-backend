import { ApplyStatus } from 'src/entity/apply/enum/apply-status.enum';
import { ApplyType } from 'src/entity/apply/enum/apply-type.enum';
import { User } from 'src/entity/user/user.entity';

export class MeetingV0GetApplyListByMeetingResponseApplyDto {
  id: number;
  type: ApplyType;
  user: User;
  /**
   * 신청 각오
   * - 모임장의 경우만 노출
   */
  content?: string;
  appliedDate: Date;
  status: ApplyStatus;
}
