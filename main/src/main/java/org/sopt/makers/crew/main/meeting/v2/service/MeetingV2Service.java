package org.sopt.makers.crew.main.meeting.v2.service;

import org.sopt.makers.crew.main.meeting.v2.dto.query.MeetingV2GetAllMeetingByOrgUserQueryDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2GetAllMeetingByOrgUserDto;

public interface MeetingV2Service {

  MeetingV2GetAllMeetingByOrgUserDto getAllMeetingByOrgUser(
      MeetingV2GetAllMeetingByOrgUserQueryDto queryDto);

}
