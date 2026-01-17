package org.sopt.makers.crew.main.soptmap.service.dto;

import java.util.List;

import org.sopt.makers.crew.main.entity.soptmap.MapTag;
import org.sopt.makers.crew.main.entity.soptmap.SoptMap;
import org.sopt.makers.crew.main.entity.user.User;

import com.querydsl.core.annotations.QueryProjection;

import lombok.Getter;

@Getter
public class SoptMapWithRecommendInfo {
	private final Long id;
	private final String placeName;
	private final String description;
	private final List<MapTag> mapTags;
	private final List<Long> nearbyStationIds;
	private final Long recommendCount; // active=true인 추천 수
	private final Boolean isRecommended; // 현재 유저의 추천 여부
	private final String kakaoLink;
	private final String naverLink;
	private final User user;

	@QueryProjection
	public SoptMapWithRecommendInfo(
		SoptMap soptMap,
		Long recommendCount,
		Boolean isRecommended,
		User user) {
		this.id = soptMap.getId();
		this.placeName = soptMap.getPlaceName();
		this.description = soptMap.getDescription();
		this.mapTags = soptMap.getMapTags();
		this.nearbyStationIds = soptMap.getNearbyStationIds();
		this.recommendCount = getRecommendCountOrDefault(recommendCount);
		this.isRecommended = getIsRecommendedOrDefault(isRecommended);
		this.kakaoLink = soptMap.getKakaoLink();
		this.naverLink = soptMap.getNaverLink();
		this.user = user;
	}

	private Long getRecommendCountOrDefault(Long recommendCount) {
		if (recommendCount == null) {
			return 0L;
		}
		return recommendCount;
	}

	private Boolean getIsRecommendedOrDefault(Boolean isRecommended) {
		if (isRecommended == null) {
			return false;
		}
		return isRecommended;
	}
}
