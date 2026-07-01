package org.sopt.makers.crew.main.meetingdemand.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "모임 수요 댓글 수정 request body dto")
public class MeetingDemandCommentV2UpdateCommentBodyDto {

	@Schema(example = "수정된 댓글 내용입니다.", description = "댓글 내용")
	@NotEmpty
	private String contents;
}
