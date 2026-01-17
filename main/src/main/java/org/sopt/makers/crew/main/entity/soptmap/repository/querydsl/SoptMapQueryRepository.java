package org.sopt.makers.crew.main.entity.soptmap.repository.querydsl;

import java.util.List;
import java.util.Optional;

import org.sopt.makers.crew.main.entity.soptmap.MapTag;
import org.sopt.makers.crew.main.soptmap.dto.SortType;
import org.sopt.makers.crew.main.soptmap.service.dto.SoptMapWithRecommendInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SoptMapQueryRepository {

	/**
	 * SoptMap 목록 조회 (페이지네이션, 필터링, 정렬 지원)
	 *
	 * @param userId     현재 로그인한 유저 ID (추천 여부 확인용)
	 * @param categories 필터링할 MapTag (nullable)
	 * @param sortType   정렬 타입 (LATEST, POPULAR)
	 * @param stationIds 필터링할 지하철역 ID 리스트 (nullable)
	 * @param pageable   페이지네이션 정보
	 * @return Page<SoptMapWithRecommendInfo>
	 */
	Page<SoptMapWithRecommendInfo> searchSoptMap(
		Long userId,
		List<MapTag> categories,
		SortType sortType,
		List<Long> stationIds,
		Pageable pageable);

	/**
	 * SoptMap 단건 조회 (추천 정보 포함)
	 *
	 * @param userId    현재 로그인한 유저 ID (추천 여부 확인용)
	 * @param soptMapId 조회할 SoptMap ID
	 * @return Optional<SoptMapWithRecommendInfo>
	 */
	Optional<SoptMapWithRecommendInfo> findSoptMapWithRecommendInfo(Long userId, Long soptMapId);
}
