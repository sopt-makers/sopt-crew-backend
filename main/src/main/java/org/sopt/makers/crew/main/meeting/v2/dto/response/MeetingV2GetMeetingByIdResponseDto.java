package org.sopt.makers.crew.main.meeting.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.sopt.makers.crew.main.entity.meeting.enums.EnMeetingStatus;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.entity.meeting.vo.ImageUrlVO;

@Getter
@AllArgsConstructor(staticName = "of")
@Schema(description = "모임 상세 조회 dto")
public class MeetingV2GetMeetingByIdResponseDto {
    Integer meetingId;
    String title;
    String category;
    List<ImageUrlVO> imageURLs;
    LocalDateTime applyStartDate;
    LocalDateTime applyEndDate;
    int capacity;
    String desc;
    String processDesc;
    LocalDateTime activityStartDate;
    LocalDateTime activityEndDate;
    String leaderDesc;
    String targetDesc;
    String note;
    Boolean isMentorNeeded;
    Boolean canJoinOnlyActiveGeneration;
    Integer createdGeneration;
    Integer targetActiveGeneration;
    MeetingJoinablePart[] joinableParts;

    EnMeetingStatus status;
    Integer approvedApplyCount;
    Boolean host;
    Boolean apply;
    Boolean approved;

    MeetingCreatorDto meetingCreator;

    List<ApplyInfoDto> appliedInfo;

}
