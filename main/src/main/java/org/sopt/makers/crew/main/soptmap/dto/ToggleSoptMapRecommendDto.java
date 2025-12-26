package org.sopt.makers.crew.main.soptmap.dto;

import org.sopt.makers.crew.main.entity.soptmap.MapRecommend;

public record ToggleSoptMapRecommendDto(Long soptMapId, boolean isRecommended) {

	public static ToggleSoptMapRecommendDto from(MapRecommend mapRecommend) {
		return new ToggleSoptMapRecommendDto(mapRecommend.getSoptMapId(), mapRecommend.getActive());
	}

	public static ToggleSoptMapRecommendDto of(Long soptMapId, boolean isRecommended) {
		return new ToggleSoptMapRecommendDto(soptMapId, isRecommended);
	}
}
