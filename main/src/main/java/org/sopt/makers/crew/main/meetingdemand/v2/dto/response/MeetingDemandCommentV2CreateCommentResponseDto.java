package org.sopt.makers.crew.main.meetingdemand.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
@Schema(name = "MeetingDemandCommentV2CreateCommentResponseDto", description = "모임 수요 댓글 생성 응답 Dto")
public class MeetingDemandCommentV2CreateCommentResponseDto {

	@Schema(description = "생성된 댓글 id", example = "1")
	@NotNull
	private Integer commentId;
}
