package org.sopt.makers.crew.main.meetingdemand.v2.dto.query;

import org.sopt.makers.crew.main.global.pagination.dto.PageOptionsDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(name = "MeetingDemandV2GetOpenedMeetingsQueryDto", description = "모임 수요 기반 개설 모임 목록 요청 Dto")
public class MeetingDemandV2GetOpenedMeetingsQueryDto extends PageOptionsDto {

	public MeetingDemandV2GetOpenedMeetingsQueryDto(Integer page, Integer take) {
		super(page, take);
	}
}
