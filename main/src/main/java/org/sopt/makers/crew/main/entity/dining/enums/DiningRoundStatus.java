package org.sopt.makers.crew.main.entity.dining.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DiningRoundStatus {
	OPEN("진행중"),
	CLOSED("마감");

	private final String value;
}
