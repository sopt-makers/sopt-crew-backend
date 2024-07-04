package org.sopt.makers.crew.main.post.v2.dto.response;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class CommenterThumbnails {
    private final List<String> commenterThumbnails;
}
