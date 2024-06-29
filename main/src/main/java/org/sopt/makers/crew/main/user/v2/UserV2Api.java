package org.sopt.makers.crew.main.user.v2;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.util.List;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetAllMeetingByUserMeetingDto;
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetApplyByUserDto;
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetMeetingByUserDto;
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetUserOwnProfileResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseStatus;

@Tag(name = "유저")
public interface UserV2Api {

    @Operation(summary = "내가 속한 모임 조회")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "성공"),
        @ApiResponse(responseCode = "204", description = "내가 속한 모임 리스트가 없는 경우", content = @Content),
    })
    public ResponseEntity<List<UserV2GetAllMeetingByUserMeetingDto>> getAllMeetingByUser(
        Principal principal);

    @Operation(summary = "유저 상세 조회")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "성공"),
        @ApiResponse(responseCode = "400", description = "해당 유저가 없는 경우", content = @Content),
    })
    ResponseEntity<User> getUserById(Integer userId);

    @Operation(summary = "유저 본인 프로필 조회")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "성공"),
        @ApiResponse(responseCode = "400", description = "해당 유저가 없는 경우", content = @Content),
    })
    ResponseEntity<UserV2GetUserOwnProfileResponseDto> getUserOwnProfile(Principal principal);

    @Operation(summary = "내가 만든 모임 조회")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "성공")
    })
    ResponseEntity<UserV2GetMeetingByUserDto> getMeetingByUser(Principal principal);

    @Operation(summary = "내가 신청한 모임 조회")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "성공")
    })
    ResponseEntity<UserV2GetApplyByUserDto> getApplyByUser(Principal principal);
}
