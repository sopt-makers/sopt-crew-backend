package org.sopt.makers.crew.main.entity.advertisement.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventType {
	SOPKATHON("솝커톤"),
	NETWORKING("네트워킹");

	private final String displayName;
}
