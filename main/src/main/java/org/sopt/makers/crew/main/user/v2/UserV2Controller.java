package org.sopt.makers.crew.main.user.v2;

import java.security.Principal;
import java.util.List;

import org.sopt.makers.crew.main.global.util.UserUtil;
import org.sopt.makers.crew.main.user.v2.dto.UpdateUserInterestKeywordRequestDto;
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetAllMeetingByUserMeetingDto;
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetAllMentionUserDto;
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetAllUserDto;
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetAppliedMeetingByUserResponseDto;
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetCreatedMeetingByUserResponseDto;
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetInterestedKeywordsResponseDto;
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetUserOwnProfileResponseDto;
import org.sopt.makers.crew.main.user.v2.service.UserV2Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/user/v2")
@RequiredArgsConstructor
public class UserV2Controller implements UserV2Api {

	private final UserV2Service userV2Service;

	@GetMapping("/meeting/all")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<List<UserV2GetAllMeetingByUserMeetingDto>> getAllMeetingByUser(
		Principal principal) {
		Integer userId = UserUtil.getUserId(principal);
		return ResponseEntity.ok(userV2Service.getAllMeetingByUser(userId));
	}

	@Override
	@GetMapping("/mention")
	@Deprecated
	public ResponseEntity<List<UserV2GetAllMentionUserDto>> getAllMentionUser(
		Principal principal) {

		UserUtil.getUserId(principal);
		return ResponseEntity.ok(userV2Service.getAllMentionUser());
	}

	@Override
	@GetMapping
	public ResponseEntity<List<UserV2GetAllUserDto>> getAllUser(Principal principal) {

		return ResponseEntity.ok(userV2Service.getAllUser());
	}

	@Override
	@GetMapping("/profile/me")
	public ResponseEntity<UserV2GetUserOwnProfileResponseDto> getUserOwnProfile(Principal principal) {

		Integer userId = UserUtil.getUserId(principal);
		return ResponseEntity.ok().body(userV2Service.getUserOwnProfile(userId));
	}

	@Override
	@GetMapping("/apply")
	public ResponseEntity<UserV2GetAppliedMeetingByUserResponseDto> getAppliedMeetingByUser(Principal principal) {

		Integer userId = UserUtil.getUserId(principal);
		return ResponseEntity.ok().body(userV2Service.getAppliedMeetingByUser(userId));
	}

	@Override
	@GetMapping("/meeting")
	public ResponseEntity<UserV2GetCreatedMeetingByUserResponseDto> getCreatedMeetingByUser(Principal principal) {

		Integer userId = UserUtil.getUserId(principal);
		return ResponseEntity.ok().body(userV2Service.getCreatedMeetingByUser(userId));
	}

	@Override
	@PostMapping("/interestedKeywords")
	public ResponseEntity<Void> updateUserInterestedKeyword(Principal principal,
		@RequestBody UpdateUserInterestKeywordRequestDto dto) {
		Integer userId = UserUtil.getUserId(principal);
		userV2Service.updateInterestedKeywords(userId, dto.keywords());
		return ResponseEntity.status(HttpStatus.OK).build();
	}

	@Override
	@GetMapping("/interestedKeywords")
	public ResponseEntity<UserV2GetInterestedKeywordsResponseDto> getUserInterestedKeyword(Principal principal) {
		Integer userId = UserUtil.getUserId(principal);
		return ResponseEntity.status(HttpStatus.OK)
			.body(userV2Service.getInterestedKeywords(userId));
	}

}
