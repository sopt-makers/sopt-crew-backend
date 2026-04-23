package org.sopt.makers.crew.main.meeting.v2.service;

import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.entity.user.enums.UserPart;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.global.util.UserPartUtil;
import org.springframework.stereotype.Component;

@Component
public class MeetingPartNormalizer {

	public String normalize(String part) {
		try {
			MeetingJoinablePart meetingJoinablePart = UserPartUtil.getMeetingJoinablePart(UserPart.ofValue(part));
			if (meetingJoinablePart != null) {
				return meetingJoinablePart.name();
			}
		} catch (BadRequestException exception) {
			return part;
		}

		return part;
	}
}
