package org.sopt.makers.crew.main.meeting.v2.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import org.sopt.makers.crew.main.common.pagination.dto.PageMetaDto;

@Getter
@AllArgsConstructor(staticName = "of")
@Schema(name = "MeetingV2GetAllMeetingByOrgUserDto", description = "모임 조회 응답 Dto")
public class MeetingV2GetAllMeetingByOrgUserDto {

	@Schema(description = "모임 객체 목록", example = "")
	@NotNull
	private List<MeetingV2GetAllMeetingByOrgUserMeetingDto> meetings;

	@NotNull
	private PageMetaDto meta;

}
