package org.sopt.makers.crew.main.flash.v2.service;

import org.sopt.makers.crew.main.flash.v2.dto.request.FlashV2CreateFlashBodyDto;
import org.sopt.makers.crew.main.flash.v2.dto.response.FlashV2CreateAndUpdateResponseDto;
import org.sopt.makers.crew.main.flash.v2.dto.response.FlashV2GetFlashByMeetingIdResponseDto;

public interface FlashV2Service {
	FlashV2CreateAndUpdateResponseDto createFlash(
		FlashV2CreateFlashBodyDto requestBody, Integer userId);

	FlashV2GetFlashByMeetingIdResponseDto getFlashByMeetingId(Integer meetingId, Integer userId);
}
