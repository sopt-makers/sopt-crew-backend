package org.sopt.makers.crew.main.meeting.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "모임 지원 취소 request body dto")
public class MeetingV2ApplyMeetingCancelBodyDto {
    @Schema(example = "1", required = true, description = "지원 ID")
    @NotNull
    private Integer applyId;
}
