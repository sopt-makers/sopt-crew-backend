package org.sopt.makers.crew.main.meetingdemandcomment.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
@Schema(name = "MeetingDemandCommentV2UpdateCommentResponseDto", description = "모임 수요 댓글 수정 응답 Dto")
public class MeetingDemandCommentV2UpdateCommentResponseDto {

	@Schema(description = "수정된 댓글 id", example = "1")
	@NotNull
	private Integer commentId;

	@Schema(description = "수정된 댓글 내용", example = "수정된 댓글 내용입니다.")
	@NotNull
	private String contents;

	@Schema(description = "수정 시점", example = "2026-07-01T15:30:00")
	@NotNull
	private String updatedDate;
}
