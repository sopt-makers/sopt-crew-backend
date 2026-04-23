package org.sopt.makers.crew.main.meeting.v2.service;

import java.util.Optional;

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

	public Optional<MeetingJoinablePart> findJoinablePart(String part) {
		if (part == null) {
			return Optional.empty();
		}

		try {
			return Optional.of(MeetingJoinablePart.valueOf(normalize(part)));
		} catch (IllegalArgumentException exception) {
			return Optional.empty();
		}
	}

	public String getDisplayName(MeetingJoinablePart part) {
		return switch (part) {
			case PM -> "기획";
			case DESIGN -> "디자인";
			case IOS -> "iOS";
			case ANDROID -> "안드로이드";
			case SERVER -> "서버";
			case WEB -> "웹";
		};
	}
}
