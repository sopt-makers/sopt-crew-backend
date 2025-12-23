package org.sopt.makers.crew.main.entity.soptmap.repository.querydsl;

import static org.sopt.makers.crew.main.entity.soptmap.QSubwayStation.*;

import java.util.List;

import org.sopt.makers.crew.main.entity.soptmap.SubwayStation;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SubwayStationQueryRepositoryImpl implements SubwayStationQueryRepository {

	private static final double SIMILARITY_THRESHOLD = 0.5;
	private static final int LIMIT_SIZE = 5;
	private final JPAQueryFactory queryFactory;

	@Override
	public List<SubwayStation> searchByKeyword(String keyword) {
		if (keyword == null) {
			return queryFactory.selectFrom(subwayStation)
				.orderBy(subwayStation.id.desc())
				.limit(LIMIT_SIZE)
				.fetch();
		}

		String trimmedKeyword = keyword.trim();
		NumberTemplate<Double> similarity = Expressions.numberTemplate(Double.class, "public.similarity({0}, {1})",
			subwayStation.name, trimmedKeyword);

		return queryFactory.selectFrom(subwayStation)
			.where(condition(trimmedKeyword, similarity))
			.orderBy(similarity.desc())
			.limit(LIMIT_SIZE)
			.fetch();
	}

	private BooleanBuilder condition(String trimmedKeyword, NumberTemplate<Double> similarity) {
		BooleanBuilder builder = new BooleanBuilder();
		builder.and(subwayStation.name.containsIgnoreCase(trimmedKeyword)
			.or(similarity.gt(SIMILARITY_THRESHOLD)));
		return builder;
	}

}
