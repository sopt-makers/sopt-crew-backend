package org.sopt.makers.crew.main.tag.v2.service;

import java.util.List;

import org.sopt.makers.crew.main.entity.tag.enums.WelcomeMessageType;
import org.sopt.makers.crew.main.tag.v2.dto.response.TagV2CreateAndUpdateFlashTagResponseDto;

public interface TagV2Service {
	TagV2CreateAndUpdateFlashTagResponseDto createFlashTag(List<String> welcomeMessageTypes,
		List<String> meetingKeywordTypes,
		Integer flashId);

	List<WelcomeMessageType> getWelcomeMessageTypesByFlashId(Integer flashId);

	TagV2CreateAndUpdateFlashTagResponseDto updateFlashTag(List<String> welcomeMessageTypes,
		List<String> meetingKeywordTypes,
		Integer flashId);
}
