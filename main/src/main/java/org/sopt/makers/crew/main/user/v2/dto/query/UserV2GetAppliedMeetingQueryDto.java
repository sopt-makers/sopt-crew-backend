package org.sopt.makers.crew.main.user.v2.dto.query;

import java.time.LocalDateTime;
import lombok.Getter;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyStatus;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyType;

@Getter
public class UserV2GetAppliedMeetingQueryDto {

    private final Integer id;
    private final EnApplyType type;
    private final UserV2GetMeetingQueryDto meeting;
    private final Integer meetingId;
    private final Integer userId;
    private final String content;
    private final LocalDateTime appliedDate;
    private final EnApplyStatus status;

    public UserV2GetAppliedMeetingQueryDto(Integer id, EnApplyType type,
        UserV2GetMeetingQueryDto meeting, Integer meetingId, Integer userId, String content,
        LocalDateTime appliedDate, EnApplyStatus status) {
        this.id = id;
        this.type = type;
        this.meeting = meeting;
        this.meetingId = meetingId;
        this.userId = userId;
        this.content = content;
        this.appliedDate = appliedDate;
        this.status = status;
    }
}
