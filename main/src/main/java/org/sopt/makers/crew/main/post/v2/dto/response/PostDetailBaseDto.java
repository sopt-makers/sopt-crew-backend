package org.sopt.makers.crew.main.post.v2.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class PostDetailBaseDto {
    private final Integer id;
    private final String title;
    private final String contents;
    private final LocalDateTime createdDate;
    private final String[] images;
    private final PostWriterInfoDto user;
    private final int likeCount;
    //* 본인이 좋아요를 눌렀는지 여부
    private final Boolean isLiked;
    private final int viewCount;
    private final int commentCount;
    private final PostMeetingDto meeting;

    @QueryProjection
    public PostDetailBaseDto(Integer id, String title, String contents, LocalDateTime createdDate, String[] images,
                             PostWriterInfoDto user, int likeCount, boolean isLiked, int viewCount, int commentCount,
                             PostMeetingDto meeting) {
        this.id = id;
        this.title = title;
        this.contents = contents;
        this.createdDate = createdDate;
        this.images = images;
        this.user = user;
        this.likeCount = likeCount;
        this.isLiked = isLiked;
        this.viewCount = viewCount;
        this.commentCount = commentCount;
        this.meeting = meeting;
    }
}
