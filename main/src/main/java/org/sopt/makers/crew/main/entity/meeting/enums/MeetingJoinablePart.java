package org.sopt.makers.crew.main.entity.meeting.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MeetingJoinablePart {
	PM("기획"),
	DESIGN("디자인"),
	IOS("iOS"),
	ANDROID("안드로이드"),
	SERVER("서버"),
	WEB("웹");

	private final String displayName;
}
