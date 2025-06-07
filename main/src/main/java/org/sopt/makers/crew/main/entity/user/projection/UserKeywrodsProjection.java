package org.sopt.makers.crew.main.entity.user.projection;

import java.util.List;

import org.sopt.makers.crew.main.entity.tag.enums.MeetingKeywordType;

public interface UserKeywrodsProjection {
	List<MeetingKeywordType> getInterestedKeywords();
}
