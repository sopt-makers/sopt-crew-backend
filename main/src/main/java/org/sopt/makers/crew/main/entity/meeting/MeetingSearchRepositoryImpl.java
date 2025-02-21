package org.sopt.makers.crew.main.entity.meeting;

import static org.sopt.makers.crew.main.entity.meeting.QMeeting.*;
import static org.sopt.makers.crew.main.entity.user.QUser.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.sopt.makers.crew.main.entity.meeting.enums.EnMeetingStatus;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.global.constant.CrewConst;
import org.sopt.makers.crew.main.global.util.Time;
import org.sopt.makers.crew.main.meeting.v2.dto.query.MeetingV2GetAllMeetingQueryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
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
	public Page<Meeting> findAllByQuery(MeetingV2GetAllMeetingQueryDto queryCommand, Pageable pageable, Time time) {

		List<Meeting> meetings = getMeetings(queryCommand, pageable, time);
		JPAQuery<Long> countQuery = getCount(queryCommand, time);

		return PageableExecutionUtils.getPage(meetings,
			PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()), countQuery::fetchFirst);
	}

	/**
	 * @param meetingIds : 조회하려는 모임 id 리스트
	 * @implSpec : meetingIds 가 null 인 경우, '지금 모집중인 모임' 반환
	 * */
	@Override
	public List<Meeting> findRecommendMeetings(List<Integer> meetingIds, Time time) {

		JPAQuery<Meeting> query = queryFactory.selectFrom(meeting)
			.innerJoin(meeting.user, user)
			.fetchJoin();

		if (meetingIds == null) {
			query.where(eqStatus(List.of(String.valueOf(EnMeetingStatus.APPLY_ABLE.getValue())), time));
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
	 * @param time         현재 시간을 제공하는 Time 객체
	 * @return 정렬 및 필터링된 모임 리스트
	 */
	private List<Meeting> getMeetings(MeetingV2GetAllMeetingQueryDto queryCommand, Pageable pageable, Time time) {
		BooleanExpression statusCondition = eqStatus(queryCommand.getStatus(), time);

		NumberExpression<Integer> statusOrder = new CaseBuilder()
			.when(meeting.startDate.loe(time.now()).and(meeting.endDate.goe(time.now()))).then(1)
			.when(meeting.startDate.after(time.now())).then(2)
			.when(meeting.endDate.before(time.now())).then(3)
			.otherwise(4);

		return queryFactory
			.selectFrom(meeting)
			.where(
				eqCategory(queryCommand.getCategory()),
				statusCondition,
				isOnlyActiveGeneration(queryCommand.getIsOnlyActiveGeneration()),
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

	private JPAQuery<Long> getCount(MeetingV2GetAllMeetingQueryDto queryCommand, Time time) {
		return queryFactory
			.select(meeting.count())
			.from(meeting)
			.where(
				eqCategory(queryCommand.getCategory()),
				eqStatus(queryCommand.getStatus(), time),
				isOnlyActiveGeneration(queryCommand.getIsOnlyActiveGeneration()),
				eqJoinableParts(queryCommand.getJoinableParts()),
				eqQuery(queryCommand.getQuery())
			);
	}

	private BooleanExpression eqCategory(List<String> categories) {

		if (categories == null || categories.isEmpty()) {
			return null;
		}

		List<MeetingCategory> categoryList = categories.stream()
			.map(MeetingCategory::ofValue)
			.collect(Collectors.toList());

		return meeting.category.in(categoryList);
	}

	private BooleanExpression eqStatus(List<String> statues, Time time) {

		if (statues == null || statues.isEmpty()) {
			return null;
		}

		List<BooleanExpression> conditions = new ArrayList<>();

		List<Integer> statuesInt = statues.stream()
			.map(Integer::parseInt)
			.toList();

		for (Integer status : statuesInt) {
			if (status == EnMeetingStatus.BEFORE_START.getValue()) {
				BooleanExpression condition = meeting.startDate.after(time.now());
				conditions.add(condition);
			} else if (status == EnMeetingStatus.APPLY_ABLE.getValue()) {
				BooleanExpression afterStartDate = meeting.startDate.loe(time.now());
				BooleanExpression beforeEndDate = meeting.endDate.goe(time.now());
				BooleanExpression condition = afterStartDate.and(beforeEndDate);
				conditions.add(condition);
			} else if (status == EnMeetingStatus.RECRUITMENT_COMPLETE.getValue()) {
				BooleanExpression condition = meeting.endDate.before(time.now());
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

	private BooleanExpression isOnlyActiveGeneration(boolean isOnlyActiveGeneration) {

		if (isOnlyActiveGeneration) {
			return meeting.canJoinOnlyActiveGeneration.eq(true)
				.and(meeting.targetActiveGeneration.eq(CrewConst.ACTIVE_GENERATION));
		}

		return null;
	}

	private BooleanExpression eqJoinableParts(MeetingJoinablePart[] joinableParts) {

		if (joinableParts == null || joinableParts.length == 0) {
			return null;
		}

		String joinablePartsToString = Arrays.stream(joinableParts)
			.map(Enum::name) // 각 요소를 큰따옴표로 감쌉니다.
			.collect(Collectors.joining(",", "'{", "}'")); // 요소들을 쉼표로 연결하고 중괄호로 감쌉니다.

		// SQL 템플릿을 사용하여 BooleanExpression 생성
		return Expressions.booleanTemplate(
			"arraycontains({0}, " + joinablePartsToString + ") || '' = 'true'",
			meeting.joinableParts,
			joinablePartsToString
		);
	}

	private BooleanExpression eqQuery(String query) {
		if (query == null) {
			return null;
		}

		return meeting.title.contains(query);
	}

}
