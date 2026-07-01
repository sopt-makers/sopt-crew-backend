package org.sopt.makers.crew.main.meetingdemandcomment.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "모임 수요 댓글 생성 request body dto")
public class MeetingDemandCommentV2CreateCommentBodyDto {

	@Schema(example = "이런 모임이 열리면 좋겠어요.", description = "댓글 내용")
	@NotEmpty
	private String contents;

	@Schema(example = "true", description = "댓글/대댓글 여부, true면 부모 댓글")
	@NotNull
	private Boolean isParent;

	@Schema(example = "3", description = "대댓글인 경우 부모 댓글 id")
	private Integer parentCommentId;

	@AssertTrue(message = "대댓글 작성 시 부모 댓글 id는 필수입니다.")
	public boolean isValidParentCommentId() {
		return Boolean.TRUE.equals(isParent) || parentCommentId != null;
	}
}
