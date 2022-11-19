import { Meeting } from 'src/meeting/meeting.entity';

export const meetingStatus = async (meeting: Meeting) => {
  const { appliedInfo, capacity, startDate, endDate } = meeting;
  const nowDate = new Date();
  const okInfo = appliedInfo.filter((apply) =>
    apply.status === 1 ? apply : false,
  );

  if (okInfo.length >= capacity) {
    return 2;
  }

  if (startDate > nowDate) {
    return 0;
  } else if (startDate <= nowDate && endDate >= nowDate) {
    return 1;
  } else {
    return 2;
  }
};
