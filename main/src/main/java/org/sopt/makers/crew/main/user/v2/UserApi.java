package org.sopt.makers.crew.main.user.v2;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.util.List;
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetAllMeetingByUserMeetingDto;
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetAllMentionUserDto;
import org.springframework.http.ResponseEntity;

@Tag(name = "사용자")
public interface UserApi {

    @Operation(summary = "내가 속한 모임 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "204", description = "내가 속한 모임 리스트가 없는 경우", content = @Content),
    })
    ResponseEntity<List<UserV2GetAllMeetingByUserMeetingDto>> getAllMeetingByUser(Principal principal);

    @Operation(summary = "멘션 사용자 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공")})
    ResponseEntity<List<UserV2GetAllMentionUserDto>> getAllMentionUser(Principal principal);
}
