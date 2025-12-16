package org.sopt.makers.crew.main.entity.soptmap.repository.querydsl;

import static org.sopt.makers.crew.main.entity.soptmap.QSubwayStation.*;

import java.util.List;

import org.sopt.makers.crew.main.entity.soptmap.SubwayStation;
import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SubwayStationQueryRepository {

	private static final double SIMILARITY_THRESHOLD = 0.5;
	private static final int LIMIT_SIZE = 5;
	private final JPAQueryFactory queryFactory;

	public List<SubwayStation> searchByKeyword(String keyword) {

		NumberTemplate<Double> similarity = Expressions.numberTemplate(Double.class, "public.similarity({0}, {1})",
			subwayStation.name, keyword);

		return queryFactory.selectFrom(subwayStation)
			.where(condition(keyword, similarity))
			.orderBy(keyword != null ? similarity.desc() : subwayStation.id.desc())
			.limit(LIMIT_SIZE)
			.fetch();
	}

	private BooleanBuilder condition(String keyword, NumberTemplate<Double> similarity) {
		BooleanBuilder builder = new BooleanBuilder();
		if (keyword != null) {
			String trimKeyword = keyword.trim();
			builder.and(subwayStation.name.containsIgnoreCase(trimKeyword)
				.or(similarity.gt(SIMILARITY_THRESHOLD)));
		}
		return builder;
	}

}
