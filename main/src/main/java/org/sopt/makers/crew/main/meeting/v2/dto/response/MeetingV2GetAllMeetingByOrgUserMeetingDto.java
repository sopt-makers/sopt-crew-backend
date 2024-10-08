package org.sopt.makers.crew.main.meeting.v2.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
@Schema(name = "MeetingV2GetAllMeetingByOrgUserMeetingDto", description = "모임 객체 Dto")
public class MeetingV2GetAllMeetingByOrgUserMeetingDto {

	@Schema(description = "모임 id", example = "1")
	@NotNull
	private Integer id;

	@Schema(description = "모임장 여부", example = "true")
	@NotNull
	private Boolean isMeetingLeader;

	@Schema(description = "모임 제목", example = "모임 제목입니다1")
	@NotNull
	private String title;

	@Schema(description = "모임 사진", example = "[url] 형식")
	@NotNull
	private String imageUrl;

	@Schema(description = "모임 분류", example = "스터디")
	@NotNull
	private String category;

	@Schema(description = "활동 시작 날짜", example = "2024-07-31T15:30:00")
	@NotNull
	private LocalDateTime mStartDate;

	@Schema(description = "활동 종료 날짜", example = "2024-08-15T15:30:00")
	@NotNull
	private LocalDateTime mEndDate;

	@Schema(description = "모임 활성 여부", example = "true")
	@NotNull
	private Boolean isActiveMeeting;
}
