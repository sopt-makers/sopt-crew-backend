package org.sopt.makers.crew.main.lightning.v2.service;

import org.sopt.makers.crew.main.lightning.v2.dto.request.LightningV2CreateLightningBodyDto;
import org.sopt.makers.crew.main.lightning.v2.dto.response.LightningV2CreateLightningResponseDto;
import org.sopt.makers.crew.main.lightning.v2.dto.response.LightningV2GetLightningByMeetingIdResponseDto;

public interface LightningV2Service {
	LightningV2CreateLightningResponseDto createLightning(
		LightningV2CreateLightningBodyDto requestBody, Integer userId);

	LightningV2GetLightningByMeetingIdResponseDto getLightningByMeetingId(Integer meetingId, Integer userId);
}
