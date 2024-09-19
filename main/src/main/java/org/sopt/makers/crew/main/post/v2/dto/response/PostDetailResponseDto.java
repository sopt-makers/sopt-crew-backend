package org.sopt.makers.crew.main.post.v2.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
@Schema(name = "PostDetailResponseDto", description = "게시글의 기본 정보 + 댓글 썸네일 이미지 리스트 정보을 담고 있는 DTO")
public class PostDetailResponseDto {

    @Schema(description = "게시글 id", example = "1")
    @NotNull
    private final Integer id;

    @Schema(description = "게시글 제목", example = "게시글 제목입니다.")
    @NotNull
    private final String title;

    @Schema(description = "게시글 내용", example = "게시글 내용입니다.")
    @NotNull
    private final String contents;

    @Schema(description = "게시글 생성 일자", example = "2024-05-31T15:30:00")
    @NotNull
    private final LocalDateTime createdDate;

    @Schema(description = "게시글 이미지 목록", example = "[\"url1\", \"url2\"]")
    @NotNull
    private final String[] images;

    @Schema(description = "게시글 생성 유저 객체", example = "")
    @NotNull
    private final PostWriterInfoDto user;

    @Schema(description = "게시글 좋아요 수", example = "20")
    @NotNull
    private final int likeCount;

    @Schema(description = "게시글 좋아요 여부", example = "true")
    @NotNull
    private final Boolean isLiked;

    @Schema(description = "게시글 조회수", example = "200")
    @NotNull
    private final int viewCount;

    @Schema(description = "게시글 댓글 수", example = "30")
    @NotNull
    private final int commentCount;

    @Schema(description = "게시글에 해당하는 모임", example = "")
    @NotNull
    private final PostMeetingDto meeting;

    @Schema(description = "댓글 작성자 썸네일 목록", example = "[\"url1\", \"url2\"]")
    @NotNull
    private final List<String> commenterThumbnails;

    public static PostDetailResponseDto of(PostDetailBaseDto postDetail, CommenterThumbnails postTopCommenterThumbnails) {
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
                postTopCommenterThumbnails.getCommenterThumbnails()
        );
    }
}
