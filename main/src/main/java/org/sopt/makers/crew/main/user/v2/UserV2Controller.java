package org.sopt.makers.crew.main.user.v2;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.crew.main.common.util.UserUtil;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetAllMeetingByUserMeetingDto;
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetMeetingByUserDto;
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetUserOwnProfileResponseDto;
import org.sopt.makers.crew.main.user.v2.service.UserV2Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/v2")
@RequiredArgsConstructor
@Tag(name = "사용자")
public class UserV2Controller implements UserV2Api {

  private final UserV2Service userV2Service;

  @Override
  @GetMapping("/meeting/all")
  public ResponseEntity<List<UserV2GetAllMeetingByUserMeetingDto>> getAllMeetingByUser(
      Principal principal) {
    Integer userId = UserUtil.getUserId(principal);
    return ResponseEntity.ok(userV2Service.getAllMeetingByUser(userId));
  }

  @Override
  @GetMapping("/{userId}")
  public ResponseEntity<User> getUserById(@PathVariable Integer userId) {
    return ResponseEntity.ok(userV2Service.getUserById(userId));
  }

  @Override
  @GetMapping("/profile/me")
  public ResponseEntity<UserV2GetUserOwnProfileResponseDto> getUserOwnProfile(Principal principal) {
    Integer userId = UserUtil.getUserId(principal);
    return ResponseEntity.ok(userV2Service.getUserOwnProfile(userId));
  }

  @Override
  @GetMapping("/meeting")
  public ResponseEntity<UserV2GetMeetingByUserDto> getMeetingByUser(Principal principal) {
    Integer userId = UserUtil.getUserId(principal);
    return ResponseEntity.ok(userV2Service.getMeetingByUser(userId));
  }
}
