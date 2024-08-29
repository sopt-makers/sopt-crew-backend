package org.sopt.makers.crew.main.post.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
@Schema(name = "PostV2UpdatePostResponseDto", description = "게시글 수정 응답 Dto")
public class PostV2UpdatePostResponseDto {

	@Schema(description = "모임 게시글", example = "1")
	@NotNull
	private final Integer id;

	@Schema(description = "모임 게시글 제목", example = "알고보면 쓸데있는 개발 프로세스")
	@NotNull
	private final String title;

	@Schema(description = "모임 게시글 내용", example = "api가 터졌다고 ? 깃이 터졌다고?")
	@NotNull
	private final String contents;

	@Schema(description = "모임 게시글 수정 날짜", example = "2024-08-25T15:30:00")
	@NotNull
	private final String updatedDate;

	@Schema(description = "모임 게시글 이미지 리스트", example = "[\"url1\", \"url2\"]")
	private final String[] images;
}
