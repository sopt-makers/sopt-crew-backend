package org.sopt.makers.crew.main.post.v2.dto.response;

import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;
import org.sopt.makers.crew.main.entity.post.Post;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class MumuPostHomeDto {

	@Schema(description = "모임 id", example = "1")
	@NotNull
	private final Integer meetingId;
	@Schema(description = "모임 제목", example = "모임 제목입니다.")
	@NotNull
	private final String meetingTitle;
	@Schema(description = "모임 카테고리", example = "스터디")
	@NotNull
	private final MeetingCategory meetingCategory;
	@Schema(description = "피드 id", example = "피드 id입니다.")
	@NotNull
	private Integer postId;
	@Schema(description = "피드 좋아요 수", example = "피드 좋아요 수입니다.")
	@NotNull
	private Integer likeCount;
	@Schema(description = "피드 댓글 수", example = "피드 댓글 수입니다.")
	@NotNull
	private Integer commentCount;
	@Schema(description = "피드 제목", example = "피드 제목입니다.")
	@NotNull
	private String title;
	@Schema(description = "피드 게시글", example = "피드 설명글입니다.")
	@NotNull
	private String content;

	public static MumuPostHomeDto from(Post post) {
		Meeting meeting = post.getMeeting();
		return MumuPostHomeDto.builder()
			.meetingId(post.getMeetingId())
			.meetingTitle(meeting.getTitle())
			.meetingCategory(meeting.getCategory())
			.postId(post.getId())
			.likeCount(post.getLikeCount())
			.commentCount(post.getCommentCount())
			.title(post.getTitle())
			.content(post.getContents())
			.build();
	}

}
