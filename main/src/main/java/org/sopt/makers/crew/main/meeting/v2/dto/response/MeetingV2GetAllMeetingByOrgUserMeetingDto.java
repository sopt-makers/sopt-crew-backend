package org.sopt.makers.crew.main.meeting.v2.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class MeetingV2GetAllMeetingByOrgUserMeetingDto {

  private Integer id;
  private Boolean isMeetingLeader;
  private String title;
  private String imageUrl;
  private String category;
  private LocalDateTime mStartDate;
  private LocalDateTime mEndDate;
  private Boolean isActiveMeeting;
}
