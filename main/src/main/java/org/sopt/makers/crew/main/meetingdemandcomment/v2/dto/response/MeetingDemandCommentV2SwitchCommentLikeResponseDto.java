package org.sopt.makers.crew.main.meetingdemandcomment.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
@Schema(name = "MeetingDemandCommentV2SwitchCommentLikeResponseDto", description = "모임 수요 댓글 좋아요 토글 응답 Dto")
public class MeetingDemandCommentV2SwitchCommentLikeResponseDto {

	@Schema(description = "요청 후 내가 좋아요를 누른 상태", example = "false")
	@NotNull
	private Boolean isLiked;
}
