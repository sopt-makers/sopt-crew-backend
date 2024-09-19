package org.sopt.makers.crew.main.post.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
@Schema(name = "PostDetailWithBlockStatusResponseDto", description = "게시글 객체 + 차단된 유저의 게시물 여부 true/false Dto")
public class PostDetailWithBlockStatusResponseDto {

    @Schema(description = "게시글의 기본 정보 + 댓글 썸네일 이미지 리스트 정보을 담고 있는 DTO", example = "")
    @NotNull
    private final PostDetailResponseDto postDetailResponseDto;

    @Schema(description = "차단된 유저의 게시물인지 여부", example = "false")
    @NotNull
    private final boolean isBlockedPost;
}