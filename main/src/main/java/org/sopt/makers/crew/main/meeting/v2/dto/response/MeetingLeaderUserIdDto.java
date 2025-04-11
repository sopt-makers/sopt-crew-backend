package org.sopt.makers.crew.main.meeting.v2.dto.response;

import org.sopt.makers.crew.main.entity.meeting.Meeting;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "MeetingLeaderUserIdDto", description = "모임장 Id Dto")
public record MeetingLeaderUserIdDto(
	@Schema(example = "1", description = "모임장 id")
	@NotNull
	Integer userId
) {
	public static MeetingLeaderUserIdDto from(Meeting meeting) {
		return new MeetingLeaderUserIdDto(meeting.getUserId());
	}
}
