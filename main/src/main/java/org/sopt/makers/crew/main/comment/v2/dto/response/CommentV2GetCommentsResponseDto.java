package org.sopt.makers.crew.main.comment.v2.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class CommentV2GetCommentsResponseDto {
	private final List<CommentDto> comments;
}
