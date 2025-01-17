package org.sopt.makers.crew.main.lightning.v2.service;

import org.sopt.makers.crew.main.lightning.v2.dto.request.LightningV2CreateLightningBodyDto;
import org.sopt.makers.crew.main.lightning.v2.dto.response.LightningV2CreateLightningResponseDto;

public interface LightningV2Service {
	LightningV2CreateLightningResponseDto createLightning(
		LightningV2CreateLightningBodyDto requestBody, Integer userId);
}
