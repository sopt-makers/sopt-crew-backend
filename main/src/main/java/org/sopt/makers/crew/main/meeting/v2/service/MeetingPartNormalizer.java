package org.sopt.makers.crew.main.meeting.v2.service;

import java.util.Objects;

import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.entity.user.enums.UserPart;
import org.springframework.stereotype.Component;

@Component
public class MeetingPartNormalizer {

	public String normalize(String part) {
		if (Objects.equals(part, UserPart.SERVER.getValue()) || Objects.equals(part, UserPart.BACKEND.getValue())) {
			return MeetingJoinablePart.SERVER.name();
		}

		return part;
	}
}
