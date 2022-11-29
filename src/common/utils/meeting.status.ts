import { Meeting } from 'src/meeting/meeting.entity';

export const meetingStatus = async (meeting: Meeting) => {
  const { appliedInfo, capacity, startDate, endDate } = meeting;
  const nowDate = new Date();
  const okInfo = appliedInfo.filter((apply) =>
    apply.status === 1 ? apply : false,
  );

  let status;

  if (okInfo.length >= capacity) {
    status = 2;
  }

  if (startDate > nowDate) {
    status = 0;
  } else if (startDate <= nowDate && endDate >= nowDate) {
    status = 1;
  } else {
    status = 2;
  }

  return { status, confirmedApply: okInfo };
};
