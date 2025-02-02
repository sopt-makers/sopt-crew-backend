package org.sopt.makers.crew.main.tag.v2.service;

import java.util.List;

import org.sopt.makers.crew.main.entity.tag.enums.WelcomeMessageType;
import org.sopt.makers.crew.main.tag.v2.dto.response.TagV2CreateFlashTagResponseDto;

public interface TagV2Service {
	TagV2CreateFlashTagResponseDto createFlashTag(List<String> welcomeMessageTypes, Integer flashId);

	List<WelcomeMessageType> getWelcomeMessageTypesByFlashId(Integer flashId);

	TagV2CreateFlashTagResponseDto updateFlashTag(List<String> welcomeMessageTypes, Integer flashId);
}
