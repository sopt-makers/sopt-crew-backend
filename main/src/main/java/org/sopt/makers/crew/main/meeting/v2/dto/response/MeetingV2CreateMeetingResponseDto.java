package org.sopt.makers.crew.main.meeting.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
@Schema(name = "MeetingV2CreateMeetingResponseDto", description = "모임 생성 응답 Dto")
public class MeetingV2CreateMeetingResponseDto {

    @Schema(description = "모임 id", example = "1")
    private Integer meetingId;
}
