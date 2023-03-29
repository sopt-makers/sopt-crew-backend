import { User } from 'src/users/user.entity';
import { ApplyStatus, ApplyType } from '../../apply.entity';

export class GetApplyListByMeetingResponseApplyDto {
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
