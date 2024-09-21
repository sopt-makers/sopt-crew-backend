package org.sopt.makers.crew.main.post.v2.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import org.sopt.makers.crew.main.common.pagination.dto.PageMetaDto;

@Getter
@AllArgsConstructor(staticName = "of")
@Schema(name = "PostV2GetPostsResponseDto", description = "게시글 조회 응답 Dto")
public class PostV2GetPostsResponseDto {

	@Schema(description = "게시글 객체", example = "")
	@NotNull
	private final List<PostDetailResponseDto> posts;

	@NotNull
	private final PageMetaDto meta;

}
