package org.sopt.makers.crew.main.comment.v2.dto.response;

import java.util.List;

import org.sopt.makers.crew.main.common.pagination.dto.PageMetaDto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class CommentV2GetCommentsResponseDto {
	private final List<CommentDto> comments;
	private final PageMetaDto meta;
}
