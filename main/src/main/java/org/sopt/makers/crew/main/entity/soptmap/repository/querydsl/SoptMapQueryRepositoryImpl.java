package org.sopt.makers.crew.main.entity.soptmap.repository.querydsl;

import static org.sopt.makers.crew.main.entity.soptmap.QMapRecommend.*;
import static org.sopt.makers.crew.main.entity.soptmap.QSoptMap.*;

import java.util.List;

import org.sopt.makers.crew.main.entity.soptmap.MapTag;
import org.sopt.makers.crew.main.soptmap.dto.SortType;
import org.sopt.makers.crew.main.soptmap.service.dto.QSoptMapWithRecommendInfo;
import org.sopt.makers.crew.main.soptmap.service.dto.SoptMapWithRecommendInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SoptMapQueryRepositoryImpl implements SoptMapQueryRepository {

	private static final Long DEFAULT_TOTAL_COUNT = 0L;
	private static final String SQL_CAST_TO_TEXT_TEMPLATE = "cast({0} as text)";
	private static final int RECOMMENDED_TRUE = 1;
	private static final int RECOMMENDED_FALSE = 0;
	private static final String LIKE_PATTERN_PREFIX = "%";
	private static final String JSONB_STRING_QUOTE = "\"";

	private final JPAQueryFactory queryFactory;

	@Override
	public Page<SoptMapWithRecommendInfo> searchSoptMap(
		Long userId,
		MapTag category,
		SortType sortType,
		List<Long> stationIds,
		Pageable pageable) {
		List<SoptMapWithRecommendInfo> content = fetchSoptMapList(userId, category, sortType, stationIds, pageable);
		Long total = countSoptMap(category, stationIds);

		return new PageImpl<>(content, pageable, total);
	}

	private List<SoptMapWithRecommendInfo> fetchSoptMapList(
		Long userId,
		MapTag category,
		SortType sortType,
		List<Long> stationIds,
		Pageable pageable) {
		return createBaseQuery(userId, category, sortType, stationIds)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();
	}

	private Long countSoptMap(MapTag category, List<Long> stationIds) {
		Long total = createCountQuery(category, stationIds).fetchOne();

		if (total == null) {
			return DEFAULT_TOTAL_COUNT;
		}

		return total;
	}

	private JPAQuery<SoptMapWithRecommendInfo> createBaseQuery(
		Long userId,
		MapTag category,
		SortType sortType,
		List<Long> stationIds) {
		return queryFactory
			.select(createProjection(userId))
			.from(soptMap)
			.leftJoin(mapRecommend)
			.on(buildJoinCondition())
			.where(
				categoryFilter(category),
				stationIdsFilter(stationIds))
			.groupBy(soptMap.id)
			.orderBy(getOrderSpecifier(sortType));
	}

	private QSoptMapWithRecommendInfo createProjection(Long userId) {
		return new QSoptMapWithRecommendInfo(
			soptMap,
			mapRecommend.id.count(),
			buildIsRecommendedExpression(userId));
	}

	private BooleanExpression buildJoinCondition() {
		return mapRecommend.soptMapId.eq(soptMap.id)
			.and(mapRecommend.active.isTrue());
	}

	private com.querydsl.core.types.dsl.BooleanExpression buildIsRecommendedExpression(Long userId) {
		// userId가 null이면 항상 false
		if (userId == null) {
			return Expressions.FALSE;
		}

		// max(CASE WHEN ... THEN 1 ELSE 0 END) = 1로 변환
		return new CaseBuilder()
			.when(mapRecommend.userId.eq(userId).and(mapRecommend.active.isTrue()))
			.then(RECOMMENDED_TRUE)
			.otherwise(RECOMMENDED_FALSE)
			.max()
			.eq(RECOMMENDED_TRUE);
	}

	private JPAQuery<Long> createCountQuery(MapTag category, List<Long> stationIds) {
		return queryFactory
			.select(soptMap.count())
			.from(soptMap)
			.where(
				categoryFilter(category),
				stationIdsFilter(stationIds));
	}

	private BooleanExpression categoryFilter(MapTag category) {
		if (category == null) {
			return null;
		}

		// JSONB를 text로 변환 후 LIKE로 검색
		// HQL이 @> 연산자를 지원하지 않으므로 text casting 사용
		String searchPattern = buildJsonbStringPattern(category.name());
		return Expressions.stringTemplate(
			SQL_CAST_TO_TEXT_TEMPLATE,
			soptMap.mapTags).like(searchPattern);
	}

	private String buildJsonbStringPattern(String value) {
		return LIKE_PATTERN_PREFIX + JSONB_STRING_QUOTE + value + JSONB_STRING_QUOTE + LIKE_PATTERN_PREFIX;
	}

	private OrderSpecifier<?>[] getOrderSpecifier(SortType sortType) {
		if (sortType == SortType.POPULAR) {
			return buildPopularOrderSpecifiers();
		}
		return buildLatestOrderSpecifiers();
	}

	private OrderSpecifier<?>[] buildPopularOrderSpecifiers() {
		return new OrderSpecifier<?>[] {
			mapRecommend.id.count().desc(),
			soptMap.createdTimestamp.desc()
		};
	}

	private OrderSpecifier<?>[] buildLatestOrderSpecifiers() {
		return new OrderSpecifier<?>[] {
			soptMap.createdTimestamp.desc(),
			mapRecommend.id.count().desc() // 동일 시간이면 추천순
		};
	}

	private Predicate stationIdsFilter(List<Long> stationIds) {
		if (stationIds == null || stationIds.isEmpty()) {
			return null;
		}

		BooleanBuilder conditions = new BooleanBuilder();
		for (Long stationId : stationIds) {
			conditions.or(buildStationIdMatchCondition(stationId));
		}

		return conditions;
	}

	private Predicate buildStationIdMatchCondition(Long stationId) {
		BooleanBuilder conditions = new BooleanBuilder();
		String textTemplate = SQL_CAST_TO_TEXT_TEMPLATE;

		// [1, ... (배열 시작 + 쉼표)
		conditions.or(Expressions.stringTemplate(textTemplate, soptMap.nearbyStationIds)
			.like(LIKE_PATTERN_PREFIX + "[" + stationId + ", %"));

		// [1] (단일 요소)
		conditions.or(Expressions.stringTemplate(textTemplate, soptMap.nearbyStationIds)
			.like(LIKE_PATTERN_PREFIX + "[" + stationId + "]" + LIKE_PATTERN_PREFIX));

		// ,1, (중간 요소)
		conditions.or(Expressions.stringTemplate(textTemplate, soptMap.nearbyStationIds)
			.like(LIKE_PATTERN_PREFIX + ", " + stationId + ",%"));

		// ,1] (마지막 요소)
		conditions.or(Expressions.stringTemplate(textTemplate, soptMap.nearbyStationIds)
			.like(LIKE_PATTERN_PREFIX + ", " + stationId + "]"));

		return conditions;
	}
}
