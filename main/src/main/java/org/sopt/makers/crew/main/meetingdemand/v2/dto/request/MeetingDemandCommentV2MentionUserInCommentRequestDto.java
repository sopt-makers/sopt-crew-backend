package org.sopt.makers.crew.main.meetingdemand.v2.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "모임 수요 댓글 멘션 알림 request body dto")
public class MeetingDemandCommentV2MentionUserInCommentRequestDto {

	@Schema(example = "1", description = "모임 수요 id")
	@NotNull
	private Integer meetingDemandId;

	@Schema(example = "@배부른 상어 대댓글 알림 테스트", description = "멘션이 포함된 댓글 내용")
	@NotEmpty
	private String content;

	@Schema(example = "[1, 2]", description = "멘션된 사용자 orgId 목록")
	@NotEmpty
	private List<Long> orgIds;
}
