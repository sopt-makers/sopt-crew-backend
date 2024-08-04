package org.sopt.makers.crew.main.user.v2.service;

import java.util.List;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetAllMeetingByUserMeetingDto;
<<<<<<< HEAD
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetApplyByUserDto;
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetMeetingByUserDto;
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetUserOwnProfileResponseDto;
=======
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetAllMentionUserDto;
>>>>>>> develop

public interface UserV2Service {

  List<UserV2GetAllMeetingByUserMeetingDto> getAllMeetingByUser(Integer userId);
  User getUserById(Integer userId);
  UserV2GetUserOwnProfileResponseDto getUserOwnProfile(Integer userId);
  UserV2GetMeetingByUserDto getMeetingByUser(Integer userId);
  UserV2GetApplyByUserDto getApplyByUser(Integer userId);

  List<UserV2GetAllMentionUserDto> getAllMentionUser();
}
