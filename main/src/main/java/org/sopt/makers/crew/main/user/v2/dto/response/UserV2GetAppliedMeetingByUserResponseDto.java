package org.sopt.makers.crew.main.user.v2.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.sopt.makers.crew.main.apply.v2.dto.query.ApplyV2GetAppliedMeetingByUserQueryDto;

@Getter
@AllArgsConstructor(staticName = "of")
public class UserV2GetAppliedMeetingByUserResponseDto {

  List<ApplyV2GetAppliedMeetingByUserQueryDto> applies;
  Integer count;
}
