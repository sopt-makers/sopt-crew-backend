package org.sopt.makers.crew.main.meetingdemand.v2.service;

import org.sopt.makers.crew.main.meetingdemand.v2.dto.query.MeetingDemandV2GetMeetingDemandsQueryDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.request.MeetingDemandV2CreateMeetingDemandBodyDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.response.MeetingDemandV2CreateMeetingDemandResponseDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.response.MeetingDemandV2GetMeetingDemandResponseDto;
import org.sopt.makers.crew.main.meetingdemand.v2.dto.response.MeetingDemandV2GetMeetingDemandsResponseDto;

public interface MeetingDemandV2Service {

	MeetingDemandV2GetMeetingDemandsResponseDto getMeetingDemands(
		MeetingDemandV2GetMeetingDemandsQueryDto queryDto, Integer userId);

	MeetingDemandV2GetMeetingDemandResponseDto getMeetingDemand(Integer meetingDemandId, Integer userId);

	MeetingDemandV2CreateMeetingDemandResponseDto createMeetingDemand(
		MeetingDemandV2CreateMeetingDemandBodyDto requestBody, Integer userId);

	void deleteMeetingDemand(Integer meetingDemandId, Integer userId);
}
