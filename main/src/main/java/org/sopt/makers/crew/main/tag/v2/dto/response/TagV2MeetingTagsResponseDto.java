package org.sopt.makers.crew.main.tag.v2.dto.response;

import java.util.List;

import org.sopt.makers.crew.main.entity.tag.enums.MeetingKeywordType;
import org.sopt.makers.crew.main.entity.tag.enums.WelcomeMessageType;

public record TagV2MeetingTagsResponseDto(
	List<MeetingKeywordType> meetingKeywordTypes,
	List<WelcomeMessageType> welcomeMessageTypes
) {
	public static TagV2MeetingTagsResponseDto of(
		List<MeetingKeywordType> meetingKeywordTypes,
		List<WelcomeMessageType> welcomeMessageTypes
	) {
		return new TagV2MeetingTagsResponseDto(meetingKeywordTypes, welcomeMessageTypes);
	}
}
