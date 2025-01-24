package org.sopt.makers.crew.main.entity.flash.enums;

import lombok.Getter;

@Getter
public enum FlashTimingType {
	IMMEDIATE("당일"),
	AFTER_DISCUSSION("예정 기간(협의 후 결정)");

	private final String value;

	FlashTimingType(String value) {
		this.value = value;
	}

	public static FlashTimingType ofValue(String dbData) {
		for (FlashTimingType timing : FlashTimingType.values()) {
			if (timing.getValue().equals(dbData)) {
				return timing;
			}
		}
		throw new IllegalArgumentException("Invalid FlashTimingType value: " + dbData);
	}
}
