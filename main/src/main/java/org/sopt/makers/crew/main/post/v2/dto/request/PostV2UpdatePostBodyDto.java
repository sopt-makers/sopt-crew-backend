package org.sopt.makers.crew.main.post.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "게시물 수정 request body dto")
public class PostV2UpdatePostBodyDto {

    @Schema(description = "모임 게시글 제목", example = "알고보면 쓸데있는 개발 프로세스")
    @NotNull
    private final String title;

    @Schema(description = "모임 게시글 내용", example = "api가 터졌다고 ? 깃이 터졌다고?")
    @NotNull
    private final String contents;

    @Schema(description = "모임 게시글 이미지 리스트", example = "[\"url1\", \"url2\"]")
    @NotNull
    private final String[] images;
}
