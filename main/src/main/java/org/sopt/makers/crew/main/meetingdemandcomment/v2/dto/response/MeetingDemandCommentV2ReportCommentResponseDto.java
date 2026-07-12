package org.sopt.makers.crew.main.meetingdemandcomment.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
@Schema(name = "MeetingDemandCommentV2ReportCommentResponseDto", description = "모임 수요 댓글 신고 응답 Dto")
public class MeetingDemandCommentV2ReportCommentResponseDto {

	@Schema(description = "생성된 신고 id", example = "1")
	@NotNull
	private final Integer reportId;
}
