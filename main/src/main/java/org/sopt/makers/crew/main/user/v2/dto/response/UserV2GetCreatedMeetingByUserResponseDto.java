package org.sopt.makers.crew.main.user.v2.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.sopt.makers.crew.main.meeting.v2.dto.query.MeetingV2GetCreatedMeetingByUserQueryDto;

@Getter
@AllArgsConstructor(staticName = "of")
public class UserV2GetCreatedMeetingByUserResponseDto {

  List<MeetingV2GetCreatedMeetingByUserQueryDto> meetings;
  Integer count;
}
