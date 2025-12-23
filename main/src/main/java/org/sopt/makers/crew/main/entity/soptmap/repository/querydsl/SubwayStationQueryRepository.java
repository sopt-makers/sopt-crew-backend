package org.sopt.makers.crew.main.entity.soptmap.repository.querydsl;

import java.util.List;

import org.sopt.makers.crew.main.entity.soptmap.SubwayStation;

public interface SubwayStationQueryRepository {

	/**
	 * 키워드 기반 지하철역 유사도 검색
	 *
	 * @param keyword 검색 키워드 (nullable)
	 * @return 유사도 높은 순으로 정렬된 지하철역 목록 (최대 5개)
	 */
	List<SubwayStation> searchByKeyword(String keyword);
}
