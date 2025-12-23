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

import com.querydsl.core.types.OrderSpecifier;
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

	private final JPAQueryFactory queryFactory;

	@Override
	public Page<SoptMapWithRecommendInfo> searchSoptMap(
		Long userId,
		MapTag category,
		SortType sortType,
		Pageable pageable) {
		List<SoptMapWithRecommendInfo> content = fetchSoptMapList(userId, category, sortType, pageable);
		Long total = countSoptMap(category);

		return new PageImpl<>(content, pageable, total);
	}

	private List<SoptMapWithRecommendInfo> fetchSoptMapList(
		Long userId,
		MapTag category,
		SortType sortType,
		Pageable pageable) {
		return createBaseQuery(userId, category, sortType)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();
	}

	private Long countSoptMap(MapTag category) {
		Long total = createCountQuery(category).fetchOne();

		if (total == null) {
			return DEFAULT_TOTAL_COUNT;
		}

		return total;
	}

	private JPAQuery<SoptMapWithRecommendInfo> createBaseQuery(
		Long userId,
		MapTag category,
		SortType sortType) {
		return queryFactory
			.select(createProjection(userId))
			.from(soptMap)
			.leftJoin(mapRecommend)
			.on(buildJoinCondition())
			.where(categoryFilter(category))
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
			.then(1)
			.otherwise(0)
			.max()
			.eq(1);
	}

	private JPAQuery<Long> createCountQuery(MapTag category) {
		return queryFactory
			.select(soptMap.count())
			.from(soptMap)
			.where(categoryFilter(category));
	}

	private BooleanExpression categoryFilter(MapTag category) {
		if (category == null) {
			return null;
		}

		// JSONB를 text로 변환 후 LIKE로 검색
		// @> 연산자는 HQL 파서 에러 발생하므로 text casting 사용
		String searchPattern = "%\"" + category.name() + "\"%";
		return Expressions.stringTemplate(
			"cast({0} as text)",
			soptMap.mapTags).like(searchPattern);
	}

	private String buildJsonbArrayValue(MapTag category) {
		return String.format("[\"%s\"]", category.name());
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
}
