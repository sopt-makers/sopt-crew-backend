package org.sopt.makers.crew.main.user.v2.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.sopt.makers.crew.main.user.v2.dto.query.UserV2GetAppliedMeetingQueryDto;

@Getter
@AllArgsConstructor(staticName = "of")
public class UserV2GetAppliedMeetingByUserResponseDto {

    List<UserV2GetAppliedMeetingQueryDto> applies;
    Integer count;
}
