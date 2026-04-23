package org.sopt.makers.crew.main.entity.advertisement.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum AdvertisementCategory {
	POST(6, true),
	MEETING(1, true),
	MEETING_TOP(1, false);

	private final int maxItems;
	private final boolean generalAdvertisement;
}
