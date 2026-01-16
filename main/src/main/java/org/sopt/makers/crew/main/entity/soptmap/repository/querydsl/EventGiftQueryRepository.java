package org.sopt.makers.crew.main.entity.soptmap.repository.querydsl;

import static org.sopt.makers.crew.main.entity.soptmap.QEventGift.*;

import java.util.Optional;

import org.sopt.makers.crew.main.entity.soptmap.EventGift;
import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class EventGiftQueryRepository {

	private final JPAQueryFactory queryFactory;

	public Optional<EventGift> findByUserId(Long userId, Long mapId) {
		return Optional.ofNullable(queryFactory.selectFrom(eventGift)
			.where(eventGift.userId.eq(userId).and(eventGift.mapId.eq(mapId))
				.and(eventGift.active.isTrue()))
			.fetchFirst());
	}
}
