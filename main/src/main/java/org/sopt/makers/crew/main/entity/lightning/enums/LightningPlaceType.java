package org.sopt.makers.crew.main.entity.lightning.enums;

import lombok.Getter;

@Getter
public enum LightningPlaceType {
	OFFLINE("오프라인"),
	ONLINE("온라인"),
	AFTER_DISCUSSION("협의 후 결정");

	private final String value;

	LightningPlaceType(String value) {
		this.value = value;
	}

	public static LightningPlaceType ofValue(String dbData) {
		for (LightningPlaceType place : LightningPlaceType.values()) {
			if (place.getValue().equals(dbData)) {
				return place;
			}
		}
		throw new IllegalArgumentException("Invalid LightningPlaceType value: " + dbData);
	}
}
