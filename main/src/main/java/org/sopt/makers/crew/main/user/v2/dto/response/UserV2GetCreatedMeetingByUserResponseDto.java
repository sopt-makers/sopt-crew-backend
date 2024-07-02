package org.sopt.makers.crew.main.user.v2.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.sopt.makers.crew.main.entity.meeting.Meeting;

@Getter
@AllArgsConstructor(staticName = "of")
public class UserV2GetCreatedMeetingByUserResponseDto {

    List<Meeting> meetings;
    Integer count;
}
