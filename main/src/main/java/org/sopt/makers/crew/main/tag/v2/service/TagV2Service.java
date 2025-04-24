package org.sopt.makers.crew.main.tag.v2.service;

import java.util.List;

import org.sopt.makers.crew.main.entity.tag.enums.WelcomeMessageType;
import org.sopt.makers.crew.main.tag.v2.dto.response.TagV2CreateAndUpdateFlashTagResponseDto;
import org.sopt.makers.crew.main.tag.v2.dto.response.TagV2CreateAndUpdateGeneralMeetingTagResponseDto;

public interface TagV2Service {
	TagV2CreateAndUpdateFlashTagResponseDto createFlashMeetingTag(List<String> welcomeMessageTypes,
		List<String> meetingKeywordTypes, Integer flashId);

	TagV2CreateAndUpdateGeneralMeetingTagResponseDto createGeneralMeetingTag(List<String> welcomeMessageTypes,
		List<String> meetingKeywordTypes, Integer meetingId);

	List<WelcomeMessageType> getWelcomeMessageTypesByFlashId(Integer flashId);

	void updateGeneralMeetingTag(List<String> welcomeMessageTypes,
		List<String> meetingKeywordTypes, Integer meetingId);

	void updateFlashMeetingTag(List<String> welcomeMessageTypes,
		List<String> meetingKeywordTypes, Integer flashId);
}
