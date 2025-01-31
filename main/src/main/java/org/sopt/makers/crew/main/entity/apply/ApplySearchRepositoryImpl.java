package org.sopt.makers.crew.main.entity.apply;

import static org.sopt.makers.crew.main.entity.meeting.QMeeting.*;
import static org.sopt.makers.crew.main.global.constant.CrewConst.*;
import static org.sopt.makers.crew.main.entity.apply.QApply.apply;
import static org.sopt.makers.crew.main.entity.user.QUser.user;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import java.math.BigDecimal;
import java.time.Year;
import java.util.List;
import java.util.Objects;

import lombok.RequiredArgsConstructor;

import org.sopt.makers.crew.main.meeting.v2.dto.query.MeetingGetAppliesQueryDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.ApplyInfoDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.QApplicantDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.QApplyInfoDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ApplySearchRepositoryImpl implements ApplySearchRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ApplyInfoDto> findApplyList(MeetingGetAppliesQueryDto queryCommand, Pageable pageable, Integer meetingId, Integer meetingCreatorId, Integer userId) {
        List<ApplyInfoDto> content = getContent(queryCommand, pageable, meetingId, meetingCreatorId, userId);
        JPAQuery<Long> countQuery = getCount(queryCommand, meetingId);

        return PageableExecutionUtils.getPage(content,
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()), countQuery::fetchFirst);
    }

    private List<ApplyInfoDto> getContent(MeetingGetAppliesQueryDto queryCommand, Pageable pageable, Integer meetingId, Integer meetingCreatorId, Integer userId) {
        boolean isStudyCreator = Objects.equals(meetingCreatorId, userId);
        return queryFactory
                .select(new QApplyInfoDto(
                        apply.id, isStudyCreator ? apply.content : Expressions.constant(""),
                        apply.appliedDate, apply.status,
                        new QApplicantDto(user.id, user.name, user.orgId, user.activities, user.profileImage,
                                user.phone)))
                .from(apply)
                .innerJoin(apply.user, user)
                .where(
                        apply.meetingId.eq(meetingId),
                        apply.status.in(queryCommand.getStatus())
                )
                .orderBy(queryCommand.getDate().equals(ORDER_DESC) ? apply.appliedDate.desc() : apply.appliedDate.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    private JPAQuery<Long> getCount(MeetingGetAppliesQueryDto queryCommand, Integer meetingId) {
        return queryFactory
                .select(apply.count())
                .from(apply)
                .where(
                        apply.meetingId.eq(meetingId),
                        apply.status.in(queryCommand.getStatus())
                );

	}

	public List<Apply> findTopFastestAppliedMeetings(Integer userId, Integer limit, Integer queryYear) {
		return queryFactory
			.selectFrom(apply)
			.innerJoin(apply.meeting, meeting).fetchJoin()
			.where(
				apply.userId.eq(userId),
				apply.appliedDate.goe(Year.of(queryYear).atDay(1).atStartOfDay()),
				apply.appliedDate.loe(Year.of(queryYear).atMonth(12).atEndOfMonth().atTime(23, 59, 59, 999_999_999))
				)
			.orderBy(
				Expressions.numberTemplate(BigDecimal.class,
					"{0} - {1}", apply.appliedDate, apply.meeting.startDate).asc()
			)
			.limit(limit)
			.fetch();
	}
}
