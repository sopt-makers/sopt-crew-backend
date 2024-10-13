package org.sopt.makers.crew.main.user.v2.service;

import java.util.List;

import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetAllMeetingByUserMeetingDto;
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetAllMentionUserDto;
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetAllUserDto;
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetAppliedMeetingByUserResponseDto;
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetCreatedMeetingByUserResponseDto;
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetUserOwnProfileResponseDto;

public interface UserV2Service {

	List<UserV2GetAllMeetingByUserMeetingDto> getAllMeetingByUser(Integer userId);

	List<UserV2GetAllMentionUserDto> getAllMentionUser();

	List<UserV2GetAllUserDto> getAllUser();

	UserV2GetUserOwnProfileResponseDto getUserOwnProfile(Integer userId);

	UserV2GetAppliedMeetingByUserResponseDto getAppliedMeetingByUser(Integer userId);

	UserV2GetCreatedMeetingByUserResponseDto getCreatedMeetingByUser(Integer userId);

	User getUserByUserId(Integer userId);
}
