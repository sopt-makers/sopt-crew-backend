package org.sopt.makers.crew.main.meeting.v2;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.crew.main.meeting.v2.dto.query.MeetingV2GetAllMeetingByOrgUserQueryDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2GetAllMeetingByOrgUserDto;
import org.sopt.makers.crew.main.meeting.v2.service.MeetingV2Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/meeting/v2")
@RequiredArgsConstructor
@Tag(name = "모임")
public class MeetingV2Controller {

  private final MeetingV2Service meetingV2Service;

  @Operation(summary = "플레이그라운드 마이페이지 내 모임 정보 조회")
  @GetMapping("/org-user")
  @ResponseStatus(HttpStatus.OK)
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "204", description = "참여했던 모임이 없습니다.", content = @Content),
  })
  @Parameters({
      @Parameter(name = "page", description = "페이지, default = 1", example = "1"),
      @Parameter(name = "take", description = "가져올 데이터 개수, default = 12", example = "50"),
      @Parameter(name = "orgUserId", description = "플레이그라운드 유저 id", example = "0")
  })
  public ResponseEntity<MeetingV2GetAllMeetingByOrgUserDto> getAllMeetingByOrgUser(
      @ModelAttribute @Parameter(hidden = true) MeetingV2GetAllMeetingByOrgUserQueryDto queryDto) {
    return ResponseEntity.ok(meetingV2Service.getAllMeetingByOrgUser(queryDto));
  }
}
