package org.sopt.makers.crew.main.meeting.v2.service;

import java.util.List;

import org.sopt.makers.crew.main.flash.v2.dto.request.FlashV2CreateAndUpdateFlashBodyWithoutWelcomeMessageDto;
import org.sopt.makers.crew.main.meeting.v2.dto.query.MeetingGetAppliesQueryDto;
import org.sopt.makers.crew.main.meeting.v2.dto.query.MeetingV2GetAllMeetingByOrgUserQueryDto;
import org.sopt.makers.crew.main.meeting.v2.dto.query.MeetingV2GetAllMeetingQueryDto;
import org.sopt.makers.crew.main.meeting.v2.dto.request.ApplyV2UpdateStatusBodyDto;
import org.sopt.makers.crew.main.meeting.v2.dto.request.MeetingV2ApplyMeetingDto;
import org.sopt.makers.crew.main.meeting.v2.dto.request.MeetingV2CreateMeetingBodyDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.AppliesCsvFileUrlResponseDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingGetApplyListResponseDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingLeaderUserIdDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2ApplyMeetingResponseDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2CreateAndUpdateMeetingForFlashResponseDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2CreateMeetingResponseDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2GetAllMeetingByOrgUserDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2GetAllMeetingDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2GetMeetingBannerResponseDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2GetMeetingByIdResponseDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2GetRecommendDto;

public interface MeetingV2Service {

	MeetingV2GetAllMeetingByOrgUserDto getAllMeetingByOrgUser(
		MeetingV2GetAllMeetingByOrgUserQueryDto queryDto);

	List<MeetingV2GetMeetingBannerResponseDto> getMeetingBanner();

	MeetingV2CreateMeetingResponseDto createMeeting(MeetingV2CreateMeetingBodyDto requestBody, Integer userId);

	MeetingV2ApplyMeetingResponseDto applyGeneralMeeting(MeetingV2ApplyMeetingDto requestBody, Integer userId);

	MeetingV2ApplyMeetingResponseDto applyEventMeeting(MeetingV2ApplyMeetingDto requestBody, Integer userId);

	MeetingV2ApplyMeetingResponseDto applyEventMeetingWithStress(MeetingV2ApplyMeetingDto requestBody, Integer userId);

	void applyMeetingCancel(Integer meetingId, Integer userId);

	MeetingGetApplyListResponseDto findApplyList(MeetingGetAppliesQueryDto queryCommand, Integer meetingId,
		Integer userId);

	MeetingV2GetAllMeetingDto getMeetings(MeetingV2GetAllMeetingQueryDto queryCommand);

	void deleteMeeting(Integer meetingId, Integer userId);

	void updateMeeting(Integer meetingId, MeetingV2CreateMeetingBodyDto requestBody, Integer userId);

	void updateApplyStatus(Integer meetingId, ApplyV2UpdateStatusBodyDto requestBody, Integer userId);

	AppliesCsvFileUrlResponseDto getAppliesCsvFileUrl(Integer meetingId, List<Integer> status, String order,
		Integer userId);

	MeetingV2GetMeetingByIdResponseDto getMeetingDetail(Integer meetingId, Integer userId);

	MeetingV2GetRecommendDto getRecommendMeetingsByIds(List<Integer> meetingIds, Integer userId);

	MeetingV2CreateAndUpdateMeetingForFlashResponseDto createMeetingForFlash(Integer userId,
		FlashV2CreateAndUpdateFlashBodyWithoutWelcomeMessageDto flashBody);

	MeetingV2CreateAndUpdateMeetingForFlashResponseDto updateMeetingForFlash(Integer meetingId, Integer userId,
		FlashV2CreateAndUpdateFlashBodyWithoutWelcomeMessageDto updatedFlashBody);

	MeetingLeaderUserIdDto getMeetingLeaderUserIdByMeetingId(Integer meetingId);

	void evictMeetingCache(Integer meetingId);

	void evictMeetingLeaderCache(Integer userId);
}
