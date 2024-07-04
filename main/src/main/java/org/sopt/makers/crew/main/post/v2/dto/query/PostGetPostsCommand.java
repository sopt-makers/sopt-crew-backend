package org.sopt.makers.crew.main.post.v2.dto.query;

import java.util.Optional;
import lombok.Getter;
import org.sopt.makers.crew.main.common.pagination.dto.PageOptionsDto;

@Getter
public class PostGetPostsCommand extends PageOptionsDto {

    private Optional<Integer> meetingId;

    public PostGetPostsCommand(Integer meetingId, Integer page, Integer take) {
        super(page, take);
        this.meetingId = Optional.ofNullable(meetingId);
    }

}
