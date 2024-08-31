package org.sopt.makers.crew.main.post.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "게시물 생성 request body dto")
public class PostV2CreatePostBodyDto {

	@Schema(example = "1", required = true, description = "모임 id")
	@NotNull
	private Integer meetingId;

	@Schema(example = "알고보면 쓸데있는 개발 프로세스", required = true, description = "모임 제목")
	@NotEmpty
	private String title;

	@Schema(
		example = "[\"https://makers-web-img.s3.ap-northeast-2.amazonaws.com/meeting/2023/04/12/7bd87736-b557-4b26-a0d5-9b09f1f1d7df\"]",
		required = true,
		description = "게시글 이미지 리스트"
	)
	@NotEmpty
	private String[] images;

	@Schema(example = "api 가 터졌다고? 깃이 터졌다고?", required = true, description = "게시글 내용")
	@NotEmpty
	private String contents;

}
