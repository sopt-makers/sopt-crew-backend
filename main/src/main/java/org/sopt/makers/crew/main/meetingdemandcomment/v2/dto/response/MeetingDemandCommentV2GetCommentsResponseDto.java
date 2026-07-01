package org.sopt.makers.crew.main.meetingdemandcomment.v2.dto.response;

import java.util.List;

import org.sopt.makers.crew.main.global.pagination.dto.PageMetaDto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
@Schema(name = "MeetingDemandCommentV2GetCommentsResponseDto", description = "모임 수요 댓글 목록 조회 응답 Dto")
public class MeetingDemandCommentV2GetCommentsResponseDto {

	@ArraySchema(schema = @Schema(implementation = MeetingDemandCommentDto.class))
	@NotNull
	private final List<MeetingDemandCommentDto> comments;

	@Schema(description = "페이지네이션")
	@NotNull
	private final PageMetaDto meta;
}
