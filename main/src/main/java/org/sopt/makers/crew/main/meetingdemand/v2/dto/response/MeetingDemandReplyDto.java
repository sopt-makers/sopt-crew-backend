package org.sopt.makers.crew.main.meetingdemand.v2.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemandComment;
import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemandCommentProfile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(name = "MeetingDemandReplyDto", description = "모임 수요 대댓글 객체 응답 Dto")
public class MeetingDemandReplyDto {

	@Schema(description = "대댓글 id", example = "1")
	@NotNull
	private final Integer id;

	@Schema(description = "대댓글 내용", example = "좋은 의견이에요.")
	@NotNull
	private final String contents;

	@Schema(description = "익명 작성자 객체")
	private final MeetingDemandCommentWriterDto writer;

	@Schema(description = "대댓글 생성 시점", example = "2026-07-01T15:30:00")
	@NotNull
	private final LocalDateTime createdDate;

	@Schema(description = "좋아요 개수", example = "3")
	@NotNull
	private final int likeCount;

	@Schema(description = "댓글 좋아요 여부", example = "true")
	@NotNull
	private final Boolean isLiked;

	@Schema(description = "본인이 작성한 댓글인지 여부", example = "true")
	@NotNull
	private final Boolean isMine;

	@Schema(description = "대댓글 순서", example = "2")
	@NotNull
	private final int order;

	@Schema(description = "차단 여부", example = "false")
	@Getter(AccessLevel.NONE)
	@NotNull
	private final boolean isBlockedComment;

	public static MeetingDemandReplyDto of(MeetingDemandComment comment, boolean isLiked, boolean isMine,
		boolean isBlockedComment, Map<Integer, MeetingDemandCommentProfile> profileMap) {
		return new MeetingDemandReplyDto(comment.getId(), comment.getContents(),
			getWriter(comment, profileMap), comment.createdTimestamp, comment.getLikeCount(), isLiked,
			isMine, comment.getOrder(), isBlockedComment);
	}

	public boolean getIsBlockedComment() {
		return isBlockedComment;
	}

	private static MeetingDemandCommentWriterDto getWriter(MeetingDemandComment comment,
		Map<Integer, MeetingDemandCommentProfile> profileMap) {
		if (comment.getUserId() == null) {
			return null;
		}

		MeetingDemandCommentProfile profile = profileMap.get(comment.getUserId());
		if (profile == null) {
			return null;
		}

		return MeetingDemandCommentWriterDto.of(profile.getAnonymousNickname(), profile.getAnonymousImageUrl());
	}
}
