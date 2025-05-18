package org.sopt.makers.crew.main.entity.meeting;

import static org.sopt.makers.crew.main.entity.meeting.QMeeting.*;
import static org.sopt.makers.crew.main.entity.tag.QTag.*;
import static org.sopt.makers.crew.main.entity.user.QUser.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.sopt.makers.crew.main.entity.meeting.enums.EnMeetingStatus;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.entity.tag.enums.MeetingKeywordType;
import org.sopt.makers.crew.main.global.pagination.PaginationType;
import org.sopt.makers.crew.main.global.util.Time;
import org.sopt.makers.crew.main.meeting.v2.dto.query.MeetingV2GetAllMeetingQueryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MeetingSearchRepositoryImpl implements MeetingSearchRepository {
	private final JPAQueryFactory queryFactory;

	/**
	 * @note: canJoinOnlyActiveGeneration 처리 유의
	 * @note: status 처리 유의
	 * */
	@Override
	public Page<Meeting> findAllByQuery(MeetingV2GetAllMeetingQueryDto queryCommand, Pageable pageable, Time time,
		Integer activeGeneration) {
		LocalDateTime now = time.now();

		List<String> categoryNames = queryCommand.getCategory();
		List<MeetingCategory> categories = List.of();

		if (categoryNames != null) {
			categories = categoryNames.stream()
				.map(MeetingCategory::ofValue)
				.toList();
		}

		if (categories.size() == 1 && categories.contains(MeetingCategory.FLASH)
			&& queryCommand.getPaginationType() == PaginationType.DEFAULT) {

			List<Meeting> flashMeetings = getFlashMeetingsForFlashCarousel(queryCommand, pageable, now,
				activeGeneration);
			JPAQuery<Long> countQuery = getCount(queryCommand, now, activeGeneration);

			return PageableExecutionUtils.getPage(flashMeetings,
				PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()), countQuery::fetchFirst);
		}

		List<Meeting> meetings = getMeetings(queryCommand, pageable, now, activeGeneration);
		JPAQuery<Long> countQuery = getCount(queryCommand, now, activeGeneration);

		return PageableExecutionUtils.getPage(meetings,
			PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()), countQuery::fetchFirst);
	}

	/**
	 * @param meetingIds : 조회하려는 모임 id 리스트
	 * @param time: RealTime 객체
	 * @implSpec : meetingIds 가 null 인 경우, '지금 모집중인 모임' 반환
	 * */
	@Override
	public List<Meeting> findRecommendMeetings(List<Integer> meetingIds, Time time) {
		LocalDateTime now = time.now();
		JPAQuery<Meeting> query = queryFactory.selectFrom(meeting)
			.innerJoin(meeting.user, user)
			.fetchJoin();

		if (meetingIds == null) {
			query.where(eqStatus(List.of(String.valueOf(EnMeetingStatus.APPLY_ABLE.getValue())), now));
			return query.fetch();
		}

		query.where(meeting.id.in(meetingIds));
		return query.fetch();
	}

	/**
	 * 특정 조건을 기반으로 모집된 모임(Meeting) 리스트를 조회하는 메서드.
	 * 모집 상태(모집중 → 모집전 → 모집마감)별 우선순위 정렬 후 최신순으로 정렬하여 페이징 처리한다.
	 *
	 * @param queryCommand 사용자가 입력한 검색 조건을 포함하는 DTO
	 * @param pageable     페이징 처리 정보 (페이지 번호, 페이지 크기 등)
	 * @param now         현재 시간을 제공하는 LocalDateTime now 객체
	 * @return 정렬 및 필터링된 모임 리스트
	 */
	private List<Meeting> getMeetings(MeetingV2GetAllMeetingQueryDto queryCommand, Pageable pageable,
		LocalDateTime now, Integer activeGeneration) {
		BooleanExpression statusCondition = eqStatus(queryCommand.getStatus(), now);

		NumberExpression<Integer> statusOrder = new CaseBuilder()
			.when(meeting.startDate.loe(now).and(meeting.endDate.goe(now))).then(1)
			.when(meeting.startDate.after(now)).then(2)
			.when(meeting.endDate.before(now)).then(3)
			.otherwise(4);

		return queryFactory
			.selectFrom(meeting)
			.where(
				eqCategory(queryCommand.getCategory()),
				eqKeyword(queryCommand.getKeyword()),
				statusCondition,
				isOnlyActiveGeneration(queryCommand.getIsOnlyActiveGeneration(), activeGeneration),
				eqJoinableParts(queryCommand.getJoinableParts()),
				eqQuery(queryCommand.getQuery())
			)
			.innerJoin(meeting.user, user)
			.fetchJoin()
			.orderBy(
				statusOrder.asc(),
				meeting.id.desc()
			)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();
	}

	private List<Meeting> getFlashMeetingsForFlashCarousel(MeetingV2GetAllMeetingQueryDto queryCommand,
		Pageable pageable, LocalDateTime now, Integer activeGeneration) {
		BooleanExpression statusCondition = eqStatus(queryCommand.getStatus(), now);

		NumberExpression<Integer> statusOrder = new CaseBuilder()
			.when(meeting.endDate.before(now)).then(2)
			.otherwise(1);

		return queryFactory
			.selectFrom(meeting)
			.where(
				meeting.category.eq(MeetingCategory.FLASH),
				statusCondition,
				isOnlyActiveGeneration(queryCommand.getIsOnlyActiveGeneration(), activeGeneration),
				eqJoinableParts(queryCommand.getJoinableParts()),
				eqQuery(queryCommand.getQuery())
			)
			.innerJoin(meeting.user, user)
			.fetchJoin()
			.orderBy(
				statusOrder.asc(),  //  모집 마감은 가장 마지막, 모집 중 & 모집 전은 동일 우선순위
				meeting.mStartDate.asc(), //  모집 중 & 모집 전 내부에서는 mStartDate(번쩍 진행일)이 가장 가까운 날짜부터 오름차순 정렬
				meeting.id.desc() // 동일한 번쩍 진행일의 경우 최근 생성된 미팅이 먼저 나오도록 정렬
			)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();
	}

	private JPAQuery<Long> getCount(MeetingV2GetAllMeetingQueryDto queryCommand, LocalDateTime now,
		Integer activeGeneration) {
		return queryFactory
			.select(meeting.count())
			.from(meeting)
			.where(
				eqCategory(queryCommand.getCategory()),
				eqKeyword(queryCommand.getKeyword()),
				eqStatus(queryCommand.getStatus(), now),
				isOnlyActiveGeneration(queryCommand.getIsOnlyActiveGeneration(), activeGeneration),
				eqJoinableParts(queryCommand.getJoinableParts()),
				eqQuery(queryCommand.getQuery())
			);
	}

	private BooleanExpression eqCategory(List<String> categories) {
		if (categories == null || categories.isEmpty()) {
			return null;
		}

		List<MeetingCategory> meetingCategories = categories.stream()
			.map(MeetingCategory::ofValue)
			.toList();

		return meeting.category.in(meetingCategories);
	}

	private BooleanExpression eqKeyword(List<String> keywords) {
		if (keywords == null || keywords.isEmpty()) {
			return null;
		}

		String[] keywordNames = keywords.stream()
			.map(keyword -> MeetingKeywordType.ofValue(keyword).name())
			.toArray(String[]::new);

		return JPAExpressions.selectOne()
			.from(tag)
			.where(
				tag.meetingId.eq(meeting.id),
				containsAnyKeyword(tag.meetingKeywordTypes, keywordNames)
			)
			.exists();
	}

	private BooleanExpression eqStatus(List<String> statuses, LocalDateTime now) {
		if (statuses == null || statuses.isEmpty()) {
			return null;
		}

		List<BooleanExpression> conditions = new ArrayList<>();

		List<Integer> statusesInt = statuses.stream()
			.map(Integer::parseInt)
			.toList();

		for (Integer status : statusesInt) {
			if (status == EnMeetingStatus.BEFORE_START.getValue()) {
				BooleanExpression condition = meeting.startDate.after(now);
				conditions.add(condition);
			} else if (status == EnMeetingStatus.APPLY_ABLE.getValue()) {
				BooleanExpression afterStartDate = meeting.startDate.loe(now);
				BooleanExpression beforeEndDate = meeting.endDate.goe(now);
				BooleanExpression condition = afterStartDate.and(beforeEndDate);
				conditions.add(condition);
			} else if (status == EnMeetingStatus.RECRUITMENT_COMPLETE.getValue()) {
				BooleanExpression condition = meeting.endDate.before(now);
				conditions.add(condition);
			}
		}

		if (conditions.isEmpty()) {
			return null; // No valid conditions
		}

		BooleanExpression combinedCondition = conditions.get(0);
		for (int i = 1; i < conditions.size(); i++) {
			combinedCondition = combinedCondition.or(conditions.get(i));
		}

		return combinedCondition;
	}

	private BooleanExpression isOnlyActiveGeneration(boolean isOnlyActiveGeneration, Integer activeGeneration) {

		if (isOnlyActiveGeneration) {
			return meeting.canJoinOnlyActiveGeneration.eq(true)
				.and(meeting.targetActiveGeneration.eq(activeGeneration));
		}

		return null;
	}

	private BooleanExpression eqJoinableParts(MeetingJoinablePart[] joinableParts) {
		if (ObjectUtils.isEmpty(joinableParts)) {
			return null;
		}

		return Arrays.stream(joinableParts)
			.map(this::createPartCondition)
			.reduce(BooleanExpression::or)
			.orElse(null);
	}

	private BooleanExpression createPartCondition(MeetingJoinablePart part) {
		String partArray = "'{" + part.name() + "}'";

		return Expressions.booleanTemplate(
			"arraycontains({0}, " + partArray + ") || '' = 'true'",
			meeting.joinableParts
		);
	}

	private BooleanExpression eqQuery(String query) {
		if (query == null) {
			return null;
		}

		return meeting.title.contains(query);
	}

	private BooleanExpression containsAnyKeyword(Expression<?> jsonbField, String[] keywords) {
		if (keywords.length == 0) {
			return Expressions.asBoolean(true).isTrue();
		}

		BooleanExpression result = createLikeExpression(jsonbField, keywords[0]);

		for (int i = 1; i < keywords.length; i++) {
			result = result.or(createLikeExpression(jsonbField, keywords[i]));
		}

		return result;
	}

	private BooleanExpression createLikeExpression(Expression<?> jsonbField, String keyword) {
		return Expressions.booleanTemplate(
			"cast({0} as text) like {1}",
			jsonbField,
			"%\"" + keyword + "\"%"
		);
	}
}
