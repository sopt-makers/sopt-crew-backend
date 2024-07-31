package org.sopt.makers.crew.main.post.v2.dto.response;

import com.querydsl.core.annotations.QueryProjection;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;

@Getter
@Schema(name = "PostMeetingDto", description = "게시글에 대한 모임 Dto")
public class PostMeetingDto {

    @Schema(description = "모임 id", example = "1")
    private final Integer id;

    @Schema(description = "게시글 제목", example = "게시글 제목입니다.")
    private final String title;

    @Schema(description = "게시글 카테고리", example = "스터디")
    private final String category;

    @QueryProjection
    public PostMeetingDto(Integer id, String title, MeetingCategory category) {
        this.id = id;
        this.title = title;
        this.category = category.getValue();
    }
}
