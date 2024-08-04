package org.sopt.makers.crew.main.apply.v2.dto.query;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import lombok.Getter;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyStatus;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyType;
import org.sopt.makers.crew.main.meeting.v2.dto.query.MeetingV2GetCreatedMeetingByUserQueryDto;

@Getter
public class ApplyV2GetAppliedMeetingByUserQueryDto {

  private final Integer id;
  private final EnApplyType type;
  private final Integer meetingId;
  private final Integer userId;
  private final String content;
  private final LocalDateTime appliedDate;
  private final EnApplyStatus status;
  private final MeetingV2GetCreatedMeetingByUserQueryDto meeting;

  @QueryProjection
  public ApplyV2GetAppliedMeetingByUserQueryDto(Integer id, EnApplyType type, Integer meetingId,
      Integer userId, String content, LocalDateTime appliedDate, EnApplyStatus status,
      MeetingV2GetCreatedMeetingByUserQueryDto meeting) {
    this.id = id;
    this.type = type;
    this.meetingId = meetingId;
    this.userId = userId;
    this.content = content;
    this.appliedDate = appliedDate;
    this.status = status;
    this.meeting = meeting;
  }
}
