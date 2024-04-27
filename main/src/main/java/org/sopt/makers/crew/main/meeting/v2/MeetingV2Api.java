package org.sopt.makers.crew.main.meeting.v2;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.sopt.makers.crew.main.meeting.v2.dto.query.MeetingV2GetAllMeetingByOrgUserQueryDto;
import org.sopt.makers.crew.main.meeting.v2.dto.request.MeetingV2CreateMeetingBodyDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2CreateMeetingResponseDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2GetAllMeetingByOrgUserDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2GetMeetingBannerResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;


@Tag(name = "모임")
public interface MeetingV2Api {
    @Operation(summary = "플레이그라운드 마이페이지 내 모임 정보 조회")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "성공")})
    @Parameters({@Parameter(name = "page", description = "페이지, default = 1", example = "1"),
            @Parameter(name = "take", description = "가져올 데이터 개수, default = 12", example = "50"),
            @Parameter(name = "orgUserId", description = "플레이그라운드 유저 id", example = "0")})
    ResponseEntity<MeetingV2GetAllMeetingByOrgUserDto> getAllMeetingByOrgUser(
            @ModelAttribute @Parameter(hidden = true) MeetingV2GetAllMeetingByOrgUserQueryDto queryDto);

    @Operation(summary = "모임 둘러보기 조회")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "204", description = "모임이 없습니다.", content = @Content),})
    ResponseEntity<List<MeetingV2GetMeetingBannerResponseDto>> getMeetingBanner(
            Principal principal);

    @Operation(summary = "모임 생성")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "성공"),
            @ApiResponse(responseCode = "400", description = "\"이미지 파일이 없습니다.\" or \"한 개 이상의 파트를 입력해주세요\" or \"프로필을 입력해주세요\"", content = @Content),})
    ResponseEntity<MeetingV2CreateMeetingResponseDto> createMeeting(@Valid @RequestBody MeetingV2CreateMeetingBodyDto requestBody,
                                                                    Principal principal);


}
