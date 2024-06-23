package org.sopt.makers.crew.main.user.v2.service;

import java.util.List;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetAllMeetingByUserMeetingDto;
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetMeetingByUserDto;
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetUserOwnProfileResponseDto;

public interface UserV2Service {

  List<UserV2GetAllMeetingByUserMeetingDto> getAllMeetingByUser(Integer userId);
  User getUserById(Integer userId);
  UserV2GetUserOwnProfileResponseDto getUserOwnProfile(Integer userId);
  UserV2GetMeetingByUserDto getMeetingByUser(Integer userId);
}
