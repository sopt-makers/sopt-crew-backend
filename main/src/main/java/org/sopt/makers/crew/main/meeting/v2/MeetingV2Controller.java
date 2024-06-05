package org.sopt.makers.crew.main.meeting.v2;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.crew.main.common.util.UserUtil;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyStatus;
import org.sopt.makers.crew.main.entity.meeting.enums.EnMeetingStatus;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.entity.meeting.vo.ImageUrlVO;
import org.sopt.makers.crew.main.entity.user.vo.UserActivityVO;
import org.sopt.makers.crew.main.meeting.v2.dto.query.MeetingGetApplyListCommand;
import org.sopt.makers.crew.main.meeting.v2.dto.query.MeetingV2GetAllMeetingByOrgUserQueryDto;
import org.sopt.makers.crew.main.meeting.v2.dto.request.MeetingV2ApplyMeetingDto;
import org.sopt.makers.crew.main.meeting.v2.dto.request.MeetingV2CreateMeetingBodyDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.ApplicantDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.ApplyInfoDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingCreatorDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingGetApplyListResponseDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2ApplyMeetingResponseDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2CreateMeetingResponseDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2GetAllMeetingByOrgUserDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2GetMeetingBannerResponseDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2GetMeetingByIdResponseDto;
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
import org.springframework.web.bind.annotation.RequestParam;
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
                                                                        @ModelAttribute MeetingGetApplyListCommand queryCommand,
                                                                        Principal principal) {

        Integer userId = UserUtil.getUserId(principal);

        return ResponseEntity.status(HttpStatus.OK)
                .body(meetingV2Service.findApplyList(queryCommand, meetingId, userId));
    }

    @Override
    @GetMapping("/{meetingId}")
    public ResponseEntity<MeetingV2GetMeetingByIdResponseDto> getMeetingById(@PathVariable Integer meetingId,
                                                                             Principal principal) {

        ApplyInfoDto applyInfoDto = new ApplyInfoDto(1, "지원할래요", LocalDateTime.of(2024, 5, 18, 00, 00),
                EnApplyStatus.APPROVE,
                new ApplicantDto(158, "송민규", 258, List.of(new UserActivityVO("서버", 33)), "프로필 사진 링크", "010-1234-5678"));

        MeetingJoinablePart[] m = {MeetingJoinablePart.SERVER};

        MeetingV2GetMeetingByIdResponseDto dto = MeetingV2GetMeetingByIdResponseDto.of(163, "모임제목", "스터디",
                List.of(new ImageUrlVO(0,
                        "https://makers-web-img.s3.ap-northeast-2.amazonaws.com/meeting/2024/05/19/e927fa61-8782-4c94-9024-7b832853facb.png")),
                LocalDateTime.of(2024, 5, 18, 00, 00), LocalDateTime.of(2024, 5, 20, 23, 59),
                20, "모임 설명", "진행방식 설명", LocalDateTime.of(2024, 6, 18, 00, 00), LocalDateTime.of(2024, 6, 24, 00, 00),
                "스터디장 소개", "타겟설명", "비고", true, true, 34, 34, m, EnMeetingStatus.APPLY_ABLE, 2, false, true,
                true, MeetingCreatorDto.of(134, "송민규", 258), List.of(applyInfoDto));
        return ResponseEntity.ok(dto);
    }
}
