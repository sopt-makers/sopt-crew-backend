package org.sopt.makers.crew.main.user.v2.service;

import java.util.List;
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetAllMeetingByUserMeetingDto;
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetAllMentionUserDto;

public interface UserV2Service {

  List<UserV2GetAllMeetingByUserMeetingDto> getAllMeetingByUser(Integer userId);

  List<UserV2GetAllMentionUserDto> getAllMentionUser(Integer userId);
}
