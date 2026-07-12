package org.sopt.makers.crew.main.entity.meetingdemand.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MeetingDemandStatus {
	BEFORE_OPEN("개설전"),
	OPENED("개설완료");

	private final String value;
}
