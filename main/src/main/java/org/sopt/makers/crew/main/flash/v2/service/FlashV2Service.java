package org.sopt.makers.crew.main.flash.v2.service;

import org.sopt.makers.crew.main.flash.v2.dto.request.FlashV2CreateAndUpdateFlashBodyDto;
import org.sopt.makers.crew.main.flash.v2.dto.response.FlashV2CreateAndUpdateResponseDto;
import org.sopt.makers.crew.main.flash.v2.dto.response.FlashV2GetFlashByMeetingIdResponseDto;

public interface FlashV2Service {
	FlashV2CreateAndUpdateResponseDto createFlash(
		FlashV2CreateAndUpdateFlashBodyDto requestBody, Integer userId);

	FlashV2GetFlashByMeetingIdResponseDto getFlashDetail(Integer meetingId, Integer userId);

	FlashV2CreateAndUpdateResponseDto updateFlash(Integer meetingId,
		FlashV2CreateAndUpdateFlashBodyDto requestBody, Integer userId);
}
