package org.sopt.makers.crew.main.entity.flash.enums;

import lombok.Getter;

@Getter
public enum FlashPlaceType {
	OFFLINE("오프라인"),
	ONLINE("온라인"),
	AFTER_DISCUSSION("협의 후 결정");

	private final String value;

	FlashPlaceType(String value) {
		this.value = value;
	}

	public static FlashPlaceType ofValue(String dbData) {
		for (FlashPlaceType place : FlashPlaceType.values()) {
			if (place.getValue().equals(dbData)) {
				return place;
			}
		}
		throw new IllegalArgumentException("Invalid FlashPlaceType value: " + dbData);
	}
}
