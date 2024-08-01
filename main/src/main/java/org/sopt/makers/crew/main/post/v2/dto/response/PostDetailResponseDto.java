package org.sopt.makers.crew.main.post.v2.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
@Schema(name = "PostDetailResponseDto", description = "게시글 객체 Dto")
public class PostDetailResponseDto {

    @Schema(description = "게시글 id", example = "1")
    private final Integer id;

    @Schema(description = "게시글 제목", example = "게시글 제목입니다.")
    private final String title;

    @Schema(description = "게시글 내용", example = "게시글 내용입니다.")
    private final String contents;

    @Schema(description = "게시글 생성 일자", example = "2024-05-31T15:30:00")
    private final LocalDateTime createdDate;

    @Schema(description = "게시글 이미지 목록", example = "[\"url1\", \"url2\"]")
    private final String[] images;

    @Schema(description = "게시글 생성 유저 객체", example = "")
    private final PostWriterInfoDto user;

    @Schema(description = "게시글 좋아요 수", example = "20")
    private final int likeCount;

    @Schema(description = "게시글 좋아요 여부", example = "true")
    private final Boolean isLiked;

    @Schema(description = "게시글 조회수", example = "200")
    private final int viewCount;

    @Schema(description = "게시글 댓글 수", example = "30")
    private final int commentCount;

    @Schema(description = "게시글에 해당하는 모임", example = "")
    private final PostMeetingDto meeting;

    @Schema(description = "댓글 작성자 썸네일 목록", example = "[\"url1\", \"url2\"]")
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
