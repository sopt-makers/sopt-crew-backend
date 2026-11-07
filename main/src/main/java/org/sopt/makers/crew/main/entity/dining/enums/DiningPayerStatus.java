package org.sopt.makers.crew.main.entity.dining.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DiningPayerStatus {
	PENDING("승인대기"),
	APPROVED("승인");

	private final String value;
}
