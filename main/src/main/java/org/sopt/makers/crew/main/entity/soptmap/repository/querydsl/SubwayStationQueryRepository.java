package org.sopt.makers.crew.main.entity.soptmap.repository.querydsl;

import static org.sopt.makers.crew.main.entity.soptmap.QSubwayStation.*;

import java.util.List;

import org.sopt.makers.crew.main.entity.soptmap.SubwayStation;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SubwayStationQueryRepository {

	private final JPAQueryFactory queryFactory;

	public List<SubwayStation> searchByKeyword(String keyword) {

		NumberTemplate<Double> similarity = Expressions.numberTemplate(Double.class, "public.similarity({0}, {1})",
			subwayStation.name, keyword);

		BooleanExpression condition = subwayStation.name.likeIgnoreCase("%" + keyword + "%")
			.or(similarity.gt(0.6));

		return queryFactory.selectFrom(subwayStation)
			.where(condition)
			.orderBy(similarity.desc())
			.limit(5)
			.fetch();
	}

}
