package org.sopt.makers.crew.main.post.v2.dto.query;

import java.util.Optional;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.sopt.makers.crew.main.global.pagination.dto.PageOptionsDto;

@Getter
@Schema(name = "PostGetPostsCommand", description = "게시글 조회 요청 Dto")
public class PostGetPostsCommand extends PageOptionsDto {

    @Schema(description = "모임 id", example = "1")
    private Optional<Integer> meetingId;

    public PostGetPostsCommand(Integer meetingId, Integer page, Integer take) {
        super(page, take);
        this.meetingId = Optional.ofNullable(meetingId);
    }

}
