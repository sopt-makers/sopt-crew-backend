package org.sopt.makers.crew.main.meeting.v2;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.crew.main.common.util.UserUtil;
import org.sopt.makers.crew.main.meeting.v2.dto.query.MeetingGetAppliesQueryDto;
import org.sopt.makers.crew.main.meeting.v2.dto.query.MeetingV2GetAllMeetingByOrgUserQueryDto;
import org.sopt.makers.crew.main.meeting.v2.dto.request.MeetingV2ApplyMeetingDto;
import org.sopt.makers.crew.main.meeting.v2.dto.request.MeetingV2CreateMeetingBodyDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingGetApplyListResponseDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2ApplyMeetingResponseDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2CreateMeetingResponseDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2GetAllMeetingByOrgUserDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2GetMeetingBannerResponseDto;
import org.sopt.makers.crew.main.meeting.v2.service.MeetingV2Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/meeting/v2")
@RequiredArgsConstructor
public class MeetingV2Controller implements MeetingV2Api {

    private final MeetingV2Service meetingV2Service;

    @Override
    @GetMapping("/org-user")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<MeetingV2GetAllMeetingByOrgUserDto> getAllMeetingByOrgUser(
            @ModelAttribute @Parameter(hidden = true) MeetingV2GetAllMeetingByOrgUserQueryDto queryDto) {
        return ResponseEntity.ok(meetingV2Service.getAllMeetingByOrgUser(queryDto));
    }

    @Override
    @GetMapping("/banner")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<MeetingV2GetMeetingBannerResponseDto>> getMeetingBanner(
            Principal principal) {
        UserUtil.getUserId(principal);
        return ResponseEntity.ok(meetingV2Service.getMeetingBanner());
    }

    @Override
    @PostMapping
    public ResponseEntity<MeetingV2CreateMeetingResponseDto> createMeeting(
            @Valid @RequestBody MeetingV2CreateMeetingBodyDto requestBody,
            Principal principal) {
        Integer userId = UserUtil.getUserId(principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(meetingV2Service.createMeeting(requestBody, userId));
    }

    @Override
    @PostMapping("/apply")
    public ResponseEntity<MeetingV2ApplyMeetingResponseDto> applyMeeting(
            @Valid @RequestBody MeetingV2ApplyMeetingDto requestBody,
            Principal principal) {
        Integer userId = UserUtil.getUserId(principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(meetingV2Service.applyMeeting(requestBody, userId));
    }

    @Override
    @DeleteMapping("/{meetingId}/apply")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Void> applyMeetingCancel(@PathVariable Integer meetingId,
                                                   Principal principal) {
        Integer userId = UserUtil.getUserId(principal);
        meetingV2Service.applyMeetingCancel(meetingId, userId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Override
    @GetMapping("/{meetingId}/list")
    public ResponseEntity<MeetingGetApplyListResponseDto> findApplyList(@PathVariable Integer meetingId,
                                                                        @ModelAttribute MeetingGetAppliesQueryDto queryCommand,
                                                                        Principal principal) {

        Integer userId = UserUtil.getUserId(principal);

        return ResponseEntity.status(HttpStatus.OK)
                .body(meetingV2Service.findApplyList(queryCommand, meetingId, userId));
    }
}
