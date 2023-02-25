import { Meeting, MeetingStatus } from 'src/meeting/meeting.entity';
import { Apply, ApplyStatus } from 'src/meeting/apply.entity';
import * as dayjs from 'dayjs';

export const meetingStatus = async (meeting: Meeting) => {
  const { appliedInfo, capacity, startDate, endDate } = meeting;
  // const nowDate = new Date();
  const nowDate = dayjs().toDate();

  // 승인된 참가자 구하기
  const okInfo: Apply[] = appliedInfo.filter((apply) =>
    apply.status === ApplyStatus.APPROVE ? apply : false,
  );

  let status: MeetingStatus;

  if (okInfo.length >= capacity) {
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

  return { status, confirmedApply: okInfo };
};
