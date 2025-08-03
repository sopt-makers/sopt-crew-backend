package org.sopt.makers.crew.main.internal.dto;

import java.time.LocalDateTime;

import org.sopt.makers.crew.main.entity.user.vo.UserActivityVO;
import org.sopt.makers.crew.main.post.v2.dto.response.PostDetailWithPartBaseDto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;

@Tag(name = "플그 모임탭에 뜨기 위한 피드 정보에 대한 dto")
public record InternalPostResponseDto(
	@Schema(description = "피드 id", example = "1")
	@NotNull
	Integer id,
	@Schema(description = "피드 제목", example = "피드 제목입니다.")
	@NotNull
	String title,

	@Schema(description = "피드 내용", example = "피드 내용입니다.")
	@NotNull
	String contents,

	@Schema(description = "피드 생성일자", example = "2024-07-31T15:30:00")
	@NotNull
	LocalDateTime createdDate,

	@Schema(description = "피드 이미지", example = "[\"url1\", \"url2\"]")
	@NotNull
	String[] images,

	@Schema(description = "피드 작성자 객체", example = "")
	@NotNull
	InternalPostWriterDetailInfoDto user,

	@Schema(description = "피드 좋아요 갯수", example = "20")
	@NotNull
	int likeCount,

	//* 본인이 좋아요를 눌렀는지 여부
	@Schema(description = "피드 좋아요 여부", example = "true")
	@NotNull
	Boolean isLiked,

	@Schema(description = "피드 조회수", example = "30")
	@NotNull
	int viewCount,

	@Schema(description = "피드 댓글 수", example = "5")
	@NotNull
	int commentCount,

	@Schema(description = "해당 피드와 연결된 모임 Id", example = "5")
	@NotNull
	int meetingId
) {

	public static InternalPostResponseDto of(PostDetailWithPartBaseDto postDetailWithPartBaseDto,
		UserActivityVO recentActivity) {
		return new InternalPostResponseDto(
			postDetailWithPartBaseDto.getId(),
			postDetailWithPartBaseDto.getTitle(),
			postDetailWithPartBaseDto.getContents(),
			postDetailWithPartBaseDto.getCreatedDate(),
			postDetailWithPartBaseDto.getImages(),
			InternalPostWriterDetailInfoDto.of(postDetailWithPartBaseDto.getUser(), recentActivity),
			postDetailWithPartBaseDto.getLikeCount(),
			postDetailWithPartBaseDto.getIsLiked(),
			postDetailWithPartBaseDto.getViewCount(),
			postDetailWithPartBaseDto.getCommentCount(),
			postDetailWithPartBaseDto.getMeeting().getId()
		);
	}
}
