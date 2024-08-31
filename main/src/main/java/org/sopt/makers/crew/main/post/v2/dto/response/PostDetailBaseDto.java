package org.sopt.makers.crew.main.post.v2.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@Schema(name = "PostDetailBaseDto", description = "게시글 객체 Dto")
public class PostDetailBaseDto {

    @Schema(description = "게시글 id", example = "1")
    @NotNull
    private final Integer id;

    @Schema(description = "게시글 제목", example = "게시글 제목입니다.")
    @NotNull
    private final String title;

    @Schema(description = "게시글 내용", example = "게시글 내용입니다.")
    @NotNull
    private final String contents;

    @Schema(description = "게시글 생성일자", example = "2024-07-31T15:30:00")
    @NotNull
    private final LocalDateTime createdDate;

    @Schema(description = "게시글 이미지", example = "[\"url1\", \"url2\"]")
    @NotNull
    private final String[] images;

    @Schema(description = "게시글 작성자 객체", example = "")
    @NotNull
    private final PostWriterInfoDto user;

    @Schema(description = "게시글 좋아요 갯수", example = "20")
    private final int likeCount;

    //* 본인이 좋아요를 눌렀는지 여부
    @Schema(description = "게시글 좋아요 여부", example = "true")
    @NotNull
    private final Boolean isLiked;

    @Schema(description = "게시글 조회수", example = "30")
    @NotNull
    private final int viewCount;

    @Schema(description = "게시글 댓글 수", example = "5")
    @NotNull
    private final int commentCount;

    @Schema(description = "게시글에 대한 모임", example = "")
    @NotNull
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
