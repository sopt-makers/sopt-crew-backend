package org.sopt.makers.crew.main.entity.advertisement.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum AdvertisementCategory {
	POST(6),
	MEETING(1);

	private final int maxItems;
}
