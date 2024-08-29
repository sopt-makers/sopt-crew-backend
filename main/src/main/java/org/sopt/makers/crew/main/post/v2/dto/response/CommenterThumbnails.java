package org.sopt.makers.crew.main.post.v2.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class CommenterThumbnails {

	@Schema(description = "댓글 작성자들의 프로필 이미지 목록", example = "[\"url1\", \"url2\"]")
	private final List<String> commenterThumbnails;

}
