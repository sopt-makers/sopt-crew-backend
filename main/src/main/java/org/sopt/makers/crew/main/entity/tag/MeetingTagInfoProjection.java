package org.sopt.makers.crew.main.entity.tag;

import java.util.List;

import org.sopt.makers.crew.main.entity.tag.enums.MeetingKeywordType;
import org.sopt.makers.crew.main.entity.tag.enums.WelcomeMessageType;

public interface MeetingTagInfoProjection {
	Integer getMeetingId();

	List<MeetingKeywordType> getMeetingKeywordTypes();

	List<WelcomeMessageType> getWelcomeMessageTypes();
}
