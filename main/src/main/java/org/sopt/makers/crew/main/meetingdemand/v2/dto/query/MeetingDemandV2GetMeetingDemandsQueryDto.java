package org.sopt.makers.crew.main.meetingdemand.v2.dto.query;

import org.sopt.makers.crew.main.global.pagination.dto.PageOptionsDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(name = "MeetingDemandV2GetMeetingDemandsQueryDto", description = "모임 수요 목록 요청 Dto")
public class MeetingDemandV2GetMeetingDemandsQueryDto extends PageOptionsDto {

	private static final int DEFAULT_TAKE = 3;

	public MeetingDemandV2GetMeetingDemandsQueryDto(Integer page, Integer take) {
		super(page, take == null ? DEFAULT_TAKE : take);
	}
}
