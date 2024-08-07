package org.sopt.makers.crew.main.entity.apply;

import static org.sopt.makers.crew.main.entity.apply.QApply.apply;
import static org.sopt.makers.crew.main.entity.user.QUser.user;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.crew.main.apply.v2.dto.query.ApplyV2GetAppliedMeetingByUserQueryDto;
import org.sopt.makers.crew.main.apply.v2.dto.query.QApplyV2GetAppliedMeetingByUserQueryDto;
import org.sopt.makers.crew.main.meeting.v2.dto.query.MeetingGetAppliesQueryDto;
import org.sopt.makers.crew.main.meeting.v2.dto.query.QMeetingV2GetCreatedMeetingByUserQueryDto;
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
  public Page<ApplyInfoDto> findApplyList(MeetingGetAppliesQueryDto queryCommand, Pageable pageable,
      Integer meetingId, Integer meetingCreatorId, Integer userId) {
    List<ApplyInfoDto> content = getContent(queryCommand, pageable, meetingId, meetingCreatorId,
        userId);
    JPAQuery<Long> countQuery = getCount(queryCommand, meetingId);

    return PageableExecutionUtils.getPage(content,
        PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()), countQuery::fetchFirst);
  }

  @Override
  public List<ApplyV2GetAppliedMeetingByUserQueryDto> findAppliedMeetingByUser(Integer userId) {
    return queryFactory
        .select(new QApplyV2GetAppliedMeetingByUserQueryDto(
            apply.id, apply.type, apply.meetingId, apply.userId, apply.content, apply.appliedDate,
            apply.status,
            new QMeetingV2GetCreatedMeetingByUserQueryDto(
                apply.meeting.id,
                apply.meeting.user.id,
                apply.meeting.title,
                apply.meeting.category,
                apply.meeting.imageURL,
                apply.meeting.startDate,
                apply.meeting.endDate,
                apply.meeting.capacity,
                apply.meeting.desc,
                apply.meeting.processDesc,
                apply.meeting.mStartDate,
                apply.meeting.mEndDate,
                apply.meeting.leaderDesc,
                apply.meeting.targetDesc,
                apply.meeting.note,
                apply.meeting.isMentorNeeded,
                apply.meeting.canJoinOnlyActiveGeneration,
                apply.meeting.createdGeneration,
                apply.meeting.targetActiveGeneration,
                apply.meeting.joinableParts,
                apply.meeting.user
            )
        ))
        .from(apply)
        .where(apply.userId.eq(userId))
        .fetch();
  }

  private List<ApplyInfoDto> getContent(MeetingGetAppliesQueryDto queryCommand, Pageable pageable,
      Integer meetingId, Integer meetingCreatorId, Integer userId) {
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
        .orderBy(queryCommand.getDate().equals("desc") ? apply.appliedDate.desc()
            : apply.appliedDate.asc())
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
}
