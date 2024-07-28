package org.sopt.makers.crew.main.post.v2.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import java.util.List;
import lombok.Getter;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;
import org.sopt.makers.crew.main.entity.meeting.vo.ImageUrlVO;

@Getter
public class PostMeetingDto {
    private final Integer id;
    private final String title;
    private final String category;
    private final List<ImageUrlVO> imageURL;

    @QueryProjection
    public PostMeetingDto(Integer id, String title, MeetingCategory category, List<ImageUrlVO> imageURL) {
        this.id = id;
        this.title = title;
        this.category = category.getValue();
        this.imageURL = imageURL;
    }
}
