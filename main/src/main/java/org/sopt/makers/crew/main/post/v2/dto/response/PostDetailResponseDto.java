package org.sopt.makers.crew.main.post.v2.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class PostDetailResponseDto {
    private final Integer id;
    private final String title;
    private final String contents;
    private final LocalDateTime createdDate;
    private final String[] images;
    private final PostWriterInfoDto user;
    private final int likeCount;
    private final Boolean isLiked;
    private final int viewCount;
    private final int commentCount;
    private final PostMeetingDto meeting;
    private final List<String> commenterThumbnails;

    public static PostDetailResponseDto of(PostDetailBaseDto postDetail,
                                           CommenterThumbnails postTopCommenterThumbnails) {
        List<String> thumbnails = postTopCommenterThumbnails.getCommenterThumbnails();
        return PostDetailResponseDto.of(
                postDetail.getId(),
                postDetail.getTitle(),
                postDetail.getContents(),
                postDetail.getCreatedDate(),
                postDetail.getImages(),
                postDetail.getUser(),
                postDetail.getLikeCount(),
                postDetail.getIsLiked(),
                postDetail.getViewCount(),
                postDetail.getCommentCount(),
                postDetail.getMeeting(),
                thumbnails
        );
    }
}
