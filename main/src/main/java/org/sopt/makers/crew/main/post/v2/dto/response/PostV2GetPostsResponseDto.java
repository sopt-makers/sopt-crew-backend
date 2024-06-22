package org.sopt.makers.crew.main.post.v2.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.sopt.makers.crew.main.common.pagination.dto.PageMetaDto;

@Getter
@AllArgsConstructor(staticName = "of")
public class PostV2GetPostsResponseDto {

    private final List<PostDetailResponseDto> posts;
    private final PageMetaDto meta;
}
