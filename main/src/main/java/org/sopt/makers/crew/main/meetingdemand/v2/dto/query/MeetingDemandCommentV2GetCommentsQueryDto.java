package org.sopt.makers.crew.main.meetingdemand.v2.dto.query;

import org.sopt.makers.crew.main.global.pagination.dto.PageOptionsDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(name = "MeetingDemandCommentV2GetCommentsQueryDto", description = "모임 수요 댓글 목록 요청 Dto")
public class MeetingDemandCommentV2GetCommentsQueryDto extends PageOptionsDto {

	public MeetingDemandCommentV2GetCommentsQueryDto(Integer page, Integer take) {
		super(page, take);
	}
}
