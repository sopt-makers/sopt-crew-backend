package org.sopt.makers.crew.main.entity.meeting;

import static org.sopt.makers.crew.main.entity.meeting.QMeeting.meeting;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.crew.main.meeting.v2.dto.query.MeetingV2GetCreatedMeetingByUserQueryDto;
import org.sopt.makers.crew.main.meeting.v2.dto.query.QMeetingV2GetCreatedMeetingByUserQueryDto;

@RequiredArgsConstructor
public class MeetingQueryRepositoryImpl implements MeetingQueryRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<MeetingV2GetCreatedMeetingByUserQueryDto> findCreatedMeetingByUser(Integer userId) {
    return queryFactory.select(
            new QMeetingV2GetCreatedMeetingByUserQueryDto(
                meeting.id,
                meeting.user.id,
                meeting.title,
                meeting.category,
                meeting.imageURL,
                meeting.startDate,
                meeting.endDate,
                meeting.capacity,
                meeting.desc,
                meeting.processDesc,
                meeting.mStartDate,
                meeting.mEndDate,
                meeting.leaderDesc,
                meeting.targetDesc,
                meeting.note,
                meeting.isMentorNeeded,
                meeting.canJoinOnlyActiveGeneration,
                meeting.createdGeneration,
                meeting.targetActiveGeneration,
                meeting.joinableParts,
                meeting.user))
        .from(meeting)
        .where(meeting.user.id.eq(userId))
        .fetch();

  }
}
