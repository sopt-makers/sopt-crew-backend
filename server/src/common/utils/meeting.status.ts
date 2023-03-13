import { Meeting, MeetingStatus } from 'src/meeting/meeting.entity';
import { Apply, ApplyStatus } from 'src/meeting/apply.entity';
import { todayDate } from './time';

export const meetingStatus = async (meeting: Meeting) => {
  const { appliedInfo, capacity, startDate, endDate } = meeting;
  // const nowDate = new Date();
  const nowDate = todayDate();

  // 승인된 지원 정보
  const approvedApplies: Apply[] = appliedInfo.filter((apply) =>
    apply.status === ApplyStatus.APPROVE ? apply : false,
  );

  let status: MeetingStatus;

  if (approvedApplies.length >= capacity) {
    status = MeetingStatus.END;
  }

  if (startDate > nowDate) {
    // 아직 모임 시작 전
    status = MeetingStatus.PRE;
  } else if (startDate <= nowDate && endDate >= nowDate) {
    // 모임 가능
    status = MeetingStatus.POSSIBLE;
  } else {
    // 모임 종료
    status = MeetingStatus.END;
  }

  return { status, approvedApplies };
};
