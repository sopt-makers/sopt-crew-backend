package org.sopt.makers.crew.main.meeting.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.enums.EnMeetingStatus;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.entity.meeting.vo.ImageUrlVO;

@Getter
@RequiredArgsConstructor
@Schema(description = "모임 상세 조회 dto")
public class MeetingV2GetMeetingByIdResponseDto {
    private final Integer meetingId;
    private final String title;
    private final String category;
    private final List<ImageUrlVO> imageURLs;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final int capacity;
    private final String desc;
    private final String processDesc;
    private final LocalDateTime activityStartDate;
    private final LocalDateTime activityEndDate;
    private final String leaderDesc;
    private final String targetDesc;
    private final String note;
    private final Boolean isMentorNeeded;
    private final Boolean canJoinOnlyActiveGeneration;
    private final Integer createdGeneration;
    private final Integer targetActiveGeneration;
    private final MeetingJoinablePart[] joinableParts;

    private final EnMeetingStatus status;
    private final long approvedApplyCount;
    private final Boolean host;
    private final Boolean apply;
    private final Boolean approved;

    private final MeetingCreatorDto meetingCreator;

    private final List<SimpleApplyInfoResponseDto> appliedInfo;

    public static MeetingV2GetMeetingByIdResponseDto of(Meeting meeting, long approvedCount, Boolean isHost, Boolean isApply,
                                                        Boolean isApproved, MeetingCreatorDto meetingCreator,
                                                        List<SimpleApplyInfoResponseDto> appliedInfo, LocalDateTime now) {

        EnMeetingStatus meetingStatus = meeting.getMeetingStatus(now);

        return new MeetingV2GetMeetingByIdResponseDto(meeting.getId(), meeting.getTitle(),
                meeting.getCategory().getValue(), meeting.getImageURL(), meeting.getStartDate(), meeting.getEndDate(),
                meeting.getCapacity(), meeting.getDesc(), meeting.getProcessDesc(), meeting.getMStartDate(),
                meeting.getMEndDate(), meeting.getLeaderDesc(), meeting.getTargetDesc(), meeting.getNote(),
                meeting.getIsMentorNeeded(), meeting.getCanJoinOnlyActiveGeneration(), meeting.getCreatedGeneration(),
                meeting.getTargetActiveGeneration(), meeting.getJoinableParts(), meetingStatus,
                approvedCount, isHost, isApply, isApproved, meetingCreator, appliedInfo);
    }
}
