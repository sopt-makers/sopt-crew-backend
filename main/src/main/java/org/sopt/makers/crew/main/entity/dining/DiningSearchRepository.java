package org.sopt.makers.crew.main.entity.dining;

import static org.sopt.makers.crew.main.dining.v2.dto.query.DiningV2GetDiningsQueryDto.*;
import static org.sopt.makers.crew.main.entity.dining.QDiningAttendance.*;
import static org.sopt.makers.crew.main.entity.dining.QDiningRound.*;
import static org.sopt.makers.crew.main.entity.meeting.QMeeting.*;

import java.util.List;

import org.sopt.makers.crew.main.dining.v2.dto.query.DiningV2GetDiningsQueryDto;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

/**
 * 회식 메인 목록 조회 (meeting 중 category = 회식)
 * 정렬: 진행 중 우선 → 회식 일자 최신순 → 등록 최신순
 */
@Repository
@RequiredArgsConstructor
public class DiningSearchRepository {

	private final JPAQueryFactory queryFactory;

	public List<Meeting> findDinings(DiningV2GetDiningsQueryDto queryCommand, Integer userId) {
		return queryFactory
			.selectFrom(meeting)
			.where(conditions(queryCommand, userId))
			.orderBy(inProgressFirst(), meeting.mStartDate.desc(), meeting.id.desc())
			.offset((long)(queryCommand.getPage() - 1) * queryCommand.getTake())
			.limit(queryCommand.getTake())
			.fetch();
	}

	public long countDinings(DiningV2GetDiningsQueryDto queryCommand, Integer userId) {
		Long count = queryFactory
			.select(meeting.count())
			.from(meeting)
			.where(conditions(queryCommand, userId))
			.fetchOne();
		return count == null ? 0 : count;
	}

	private BooleanExpression[] conditions(DiningV2GetDiningsQueryDto queryCommand, Integer userId) {
		return new BooleanExpression[] {
			meeting.category.eq(MeetingCategory.DINING),
			eqStatus(queryCommand.getStatus()),
			eqPart(queryCommand.getPart()),
			containsTitle(queryCommand.getQuery()),
			queryCommand.isMyTab() ? isLeaderOrAttendee(userId) : null
		};
	}

	private BooleanExpression eqStatus(String status) {
		if (STATUS_IN_PROGRESS.equals(status)) {
			return meeting.diningEndedAt.isNull();
		}
		if (STATUS_ENDED.equals(status)) {
			return meeting.diningEndedAt.isNotNull();
		}
		return null;
	}

	/**
	 * 기존 모임 목록의 파트 필터와 같은 방식. 전체 파트 회식은 6개 파트를 모두 저장하므로 어떤 파트로 필터해도 포함된다.
	 */
	private BooleanExpression eqPart(MeetingJoinablePart part) {
		if (part == null) {
			return null;
		}
		return Expressions.booleanTemplate(
			"arraycontains({0}, '{" + part.name() + "}') || '' = 'true'",
			meeting.joinableParts
		);
	}

	private BooleanExpression containsTitle(String query) {
		if (!StringUtils.hasText(query)) {
			return null;
		}
		return meeting.title.contains(query.trim());
	}

	/**
	 * 내 회식: 내가 개설한 회식 + 현재 참석 중인 회차가 1개 이상인 회식
	 */
	private BooleanExpression isLeaderOrAttendee(Integer userId) {
		return meeting.userId.eq(userId)
			.or(JPAExpressions.selectOne()
				.from(diningAttendance, diningRound)
				.where(
					diningAttendance.roundId.eq(diningRound.id),
					diningRound.meetingId.eq(meeting.id),
					diningAttendance.userId.eq(userId)
				)
				.exists());
	}

	private OrderSpecifier<Integer> inProgressFirst() {
		return new CaseBuilder()
			.when(meeting.diningEndedAt.isNull()).then(0)
			.otherwise(1)
			.asc();
	}
}
