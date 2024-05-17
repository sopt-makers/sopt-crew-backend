package org.sopt.makers.crew.main.meeting.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "모임 지원 request body dto")
public class MeetingV2ApplyMeetingDto {
    @Schema(example = "4", required = true, description = "모임 ID")
    @NotNull
    private Integer meetingId;

    @Schema(example = "꼭 지원하고 싶습니다.", description = "지원 각오")
    @NotNull
    private String content;

}
