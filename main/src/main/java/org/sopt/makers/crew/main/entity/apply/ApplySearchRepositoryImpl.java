package org.sopt.makers.crew.main.entity.apply;

import static org.sopt.makers.crew.main.entity.apply.QApply.apply;
import static org.sopt.makers.crew.main.entity.user.QUser.user;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.crew.main.meeting.v2.dto.query.MeetingGetApplyListCommand;
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
    public Page<ApplyInfoDto> findApplyList(MeetingGetApplyListCommand queryCommand, Pageable pageable, Integer meetingId, Integer meetingCreatorId, Integer userId) {
        List<ApplyInfoDto> content = getContent(queryCommand, pageable, meetingId, meetingCreatorId, userId);
        JPAQuery<Long> countQuery = getCount(queryCommand, meetingId);

        return PageableExecutionUtils.getPage(content,
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()), countQuery::fetchFirst);
    }

    private List<ApplyInfoDto> getContent(MeetingGetApplyListCommand queryCommand, Pageable pageable, Integer meetingId, Integer meetingCreatorId, Integer userId) {
        boolean isStudyCreator = Objects.equals(meetingCreatorId, userId);
        return queryFactory
                .select(new QApplyInfoDto(
                        apply.id, apply.type, isStudyCreator ? apply.content : Expressions.constant(""),
                        apply.appliedDate, apply.status,
                        new QApplicantDto(user.id, user.name, user.orgId, user.activities, user.profileImage,
                                user.phone)))
                .from(apply)
                .innerJoin(apply.user, user)
                .where(
                        apply.meetingId.eq(meetingId),
                        apply.status.in(queryCommand.getStatus())
                )
                .orderBy(queryCommand.getDate().equals("desc") ? apply.appliedDate.desc() : apply.appliedDate.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    private JPAQuery<Long> getCount(MeetingGetApplyListCommand queryCommand, Integer meetingId) {
        return queryFactory
                .select(apply.count())
                .from(apply)
                .where(
                        apply.meetingId.eq(meetingId),
                        apply.status.in(queryCommand.getStatus())
                );

    }
}
