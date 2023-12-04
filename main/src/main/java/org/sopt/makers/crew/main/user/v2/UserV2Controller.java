package org.sopt.makers.crew.main.user.v2;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetAllMeetingByUserMeetingDto;
import org.sopt.makers.crew.main.user.v2.service.UserV2Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/v2")
@RequiredArgsConstructor
@Tag(name = "사용자")
public class UserV2Controller {

  private final UserV2Service userV2Service;

  @GetMapping("/meeting/all")
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity<List<UserV2GetAllMeetingByUserMeetingDto>> getAllMeetingByUser() {
    Integer userId = 267; //현재는 security 붙이기 전이라 추후 수정
    return ResponseEntity.ok(userV2Service.getAllMeetingByUser(userId));
  }
}
