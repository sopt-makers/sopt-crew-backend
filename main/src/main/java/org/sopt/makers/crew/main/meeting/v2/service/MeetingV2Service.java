package org.sopt.makers.crew.main.meeting.v2.service;

import java.util.List;
import org.sopt.makers.crew.main.meeting.v2.dto.query.MeetingGetAppliesQueryDto;
import org.sopt.makers.crew.main.meeting.v2.dto.query.MeetingV2GetAllMeetingByOrgUserQueryDto;
import org.sopt.makers.crew.main.meeting.v2.dto.request.MeetingV2ApplyMeetingDto;
import org.sopt.makers.crew.main.meeting.v2.dto.request.MeetingV2CreateMeetingBodyDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingGetApplyListResponseDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2ApplyMeetingResponseDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2CreateMeetingResponseDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2GetAllMeetingByOrgUserDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2GetMeetingBannerResponseDto;

public interface MeetingV2Service {

    MeetingV2GetAllMeetingByOrgUserDto getAllMeetingByOrgUser(
            MeetingV2GetAllMeetingByOrgUserQueryDto queryDto);

    List<MeetingV2GetMeetingBannerResponseDto> getMeetingBanner();

    MeetingV2CreateMeetingResponseDto createMeeting(MeetingV2CreateMeetingBodyDto requestBody, Integer userId);

    MeetingV2ApplyMeetingResponseDto applyMeeting(MeetingV2ApplyMeetingDto requestBody, Integer userId);

    void applyMeetingCancel(Integer meetingId, Integer userId);

    MeetingGetApplyListResponseDto findApplyList(MeetingGetAppliesQueryDto queryCommand, Integer meetingId,
                                                 Integer userId);
}
