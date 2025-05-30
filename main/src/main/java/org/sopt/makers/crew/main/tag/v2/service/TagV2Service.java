package org.sopt.makers.crew.main.tag.v2.service;

import java.util.List;
import java.util.Map;

import org.sopt.makers.crew.main.entity.tag.enums.MeetingKeywordType;
import org.sopt.makers.crew.main.entity.tag.enums.WelcomeMessageType;
import org.sopt.makers.crew.main.tag.v2.dto.response.TagV2CreateFlashTagResponseDto;
import org.sopt.makers.crew.main.tag.v2.dto.response.TagV2CreateGeneralMeetingTagResponseDto;
import org.sopt.makers.crew.main.tag.v2.dto.response.TagV2MeetingTagsResponseDto;

public interface TagV2Service {
	TagV2CreateFlashTagResponseDto createFlashMeetingTag(List<String> welcomeMessageTypes,
		List<String> meetingKeywordTypes, Integer flashId, Integer meetingId);

	TagV2CreateGeneralMeetingTagResponseDto createGeneralMeetingTag(List<String> welcomeMessageTypes,
		List<String> meetingKeywordTypes, Integer meetingId);

	List<WelcomeMessageType> getWelcomeMessageTypesByFlashId(Integer flashId);

	List<WelcomeMessageType> getWelcomeMessageTypesByMeetingId(Integer meetingId);

	List<MeetingKeywordType> getMeetingKeywordsTypesByFlashId(Integer flashId);

	List<MeetingKeywordType> getMeetingKeywordsTypesByMeetingId(Integer meetingId);

	void updateGeneralMeetingTag(List<String> welcomeMessageTypes,
		List<String> meetingKeywordTypes, Integer meetingId);

	void updateFlashMeetingTag(List<String> welcomeMessageTypes,
		List<String> meetingKeywordTypes, Integer flashId);

	Map<Integer, TagV2MeetingTagsResponseDto> getMeetingTagsByMeetingIds(List<Integer> meetingIds);
}
