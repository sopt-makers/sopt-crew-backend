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

	@Schema(description = "게시글의 기본 정보 + 댓글 썸네일 이미지 리스트 정보 + 차단된 유저의 게시물인지 아닌지 정보를 담고 있는 DTO")
	@NotNull
	private final List<PostDetailWithBlockStatusResponseDto> posts;

	@Schema(description = "페이지 메타 정보")
	@NotNull
	private final PageMetaDto meta;

}
