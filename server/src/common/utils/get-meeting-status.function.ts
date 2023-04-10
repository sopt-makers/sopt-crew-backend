import dayjs from 'dayjs';
import { Apply } from 'src/entity/apply/apply.entity';
import { ApplyStatus } from 'src/entity/apply/enum/apply-status.enum';
import { Meeting } from 'src/entity/meeting/meeting.entity';
import { MeetingV0MeetingStatus } from 'src/meeting/v0/enum/meeting-v0-meeting-status.enum';

export const getMeetingStatus = async (meeting: Meeting) => {
  const { appliedInfo, capacity, startDate, endDate } = meeting;
  const nowDate = dayjs().toDate();

  // 승인된 지원 정보
  const approvedApplies: Apply[] = appliedInfo.filter((apply) =>
    apply.status === ApplyStatus.APPROVE ? apply : false,
  );

  let status: MeetingV0MeetingStatus;

  if (approvedApplies.length >= capacity) {
    status = MeetingV0MeetingStatus.END;
  }

  if (startDate > nowDate) {
    // 아직 모임 시작 전
    status = MeetingV0MeetingStatus.PRE;
  } else if (startDate <= nowDate && endDate >= nowDate) {
    // 모임 가능
    status = MeetingV0MeetingStatus.POSSIBLE;
  } else {
    // 모임 종료
    status = MeetingV0MeetingStatus.END;
  }

  return { status, approvedApplies };
};
