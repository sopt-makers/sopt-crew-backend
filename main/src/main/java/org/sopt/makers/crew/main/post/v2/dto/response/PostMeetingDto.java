package org.sopt.makers.crew.main.post.v2.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;

@Getter
public class PostMeetingDto {
    private final Integer id;
    private final String title;
    private final String category;

    @QueryProjection
    public PostMeetingDto(Integer id, String title, MeetingCategory category) {
        this.id = id;
        this.title = title;
        this.category = category.getValue();
    }
}
