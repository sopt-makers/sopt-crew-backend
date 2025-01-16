package org.sopt.makers.crew.main.entity.lightning.enums;

import lombok.Getter;

@Getter
public enum LightningTimingType {
	IMMEDIATE("당일"),
	DURATION("기간"),
	AFTER_DISCUSSION("협의 후 결정");

	private final String value;

	LightningTimingType(String value) {
		this.value = value;
	}

	public static LightningTimingType ofValue(String dbData) {
		for (LightningTimingType timing : LightningTimingType.values()) {
			if (timing.getValue().equals(dbData)) {
				return timing;
			}
		}
		throw new IllegalArgumentException("Invalid LightningTimingType value: " + dbData);
	}
}
