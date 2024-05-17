package org.sopt.makers.crew.main.meeting.v2.service;

import java.util.List;
import org.sopt.makers.crew.main.meeting.v2.dto.query.MeetingV2GetAllMeetingByOrgUserQueryDto;
import org.sopt.makers.crew.main.meeting.v2.dto.request.MeetingV2ApplyMeetingDto;
import org.sopt.makers.crew.main.meeting.v2.dto.request.MeetingV2CreateMeetingBodyDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2CreateMeetingResponseDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2GetAllMeetingByOrgUserDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2GetMeetingBannerResponseDto;

public interface MeetingV2Service {

    MeetingV2GetAllMeetingByOrgUserDto getAllMeetingByOrgUser(
            MeetingV2GetAllMeetingByOrgUserQueryDto queryDto);

    List<MeetingV2GetMeetingBannerResponseDto> getMeetingBanner();

    MeetingV2CreateMeetingResponseDto createMeeting(MeetingV2CreateMeetingBodyDto requestBody, Integer userId);

    void applyMeeting(MeetingV2ApplyMeetingDto requestBody, Integer userId);
}
