package org.sopt.makers.crew.main.comment.v2.dto.response;

import java.util.List;

import org.sopt.makers.crew.main.common.pagination.dto.PageMetaDto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
@Schema(name = "CommentV2GetCommentsResponseDto", description = "댓글 목록 조회 응답 Dto")
public class CommentV2GetCommentsResponseDto {

	@ArraySchema(
		schema = @Schema(implementation = CommentDto.class)
	)
	@NotNull
	private final List<CommentDto> comments;

	@Schema(description = "페이지네이션", example = "")
	@NotNull
	private final PageMetaDto meta;

}
