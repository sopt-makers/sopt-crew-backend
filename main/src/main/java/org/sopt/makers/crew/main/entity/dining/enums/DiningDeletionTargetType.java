package org.sopt.makers.crew.main.entity.dining.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DiningDeletionTargetType {
	DINING("회식"),
	ROUND("회차"),
	ATTENDANCE("참석");

	private final String value;
}
