package org.sopt.makers.crew.main.internal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Schema(name = "InternalPostCreateRequestDto", description = "internal 게시물 생성 request body dto")
public record InternalPostCreateRequestDto(

	@Schema(example = "1", description = "모임 id")
	@NotNull
	Integer meetingId,

	@Schema(example = "알고보면 쓸데있는 개발 프로세스", description = "모임 제목")
	@NotEmpty
	String title,

	@Schema(
		example = "[\"https://makers-web-img.s3.ap-northeast-2.amazonaws.com/meeting/2023/04/12/7bd87736-b557-4b26-a0d5-9b09f1f1d7df\"]",
		required = true,
		description = "게시글 이미지 리스트"
	)
	String[] images,

	@Schema(example = "api 가 터졌다고? 깃이 터졌다고?", description = "게시글 내용")
	@NotEmpty
	String contents
) {
}
