package org.sopt.makers.crew.main.entity.soptmap;

import java.util.List;
import java.util.stream.Collectors;

public enum MapTag {
	/**
	 * FOOD : 맛집
	 * CAFE : 카폐
	 * ETC : 기타
	 */
	FOOD, CAFE, ETC;

	public static List<MapTag> fromName(List<String> name) {
		return name.stream().map(MapTag::valueOf).collect(Collectors.toList());
	}
}
