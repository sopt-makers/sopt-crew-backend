package org.sopt.makers.crew.main.post.v2.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class PostV2GetPostCountResponseDto {

    /**
     * 모임 게시글 개수
     */
    private Integer postCount;
}
