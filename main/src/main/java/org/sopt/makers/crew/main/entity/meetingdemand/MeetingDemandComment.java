package org.sopt.makers.crew.main.entity.meetingdemand;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.FORBIDDEN_EXCEPTION;

import org.sopt.makers.crew.main.entity.common.BaseTimeEntity;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.global.exception.ForbiddenException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(MeetingDemandComment.MeetingDemandCommentListener.class)
@Table(name = "meeting_demand_comment")
public class MeetingDemandComment extends BaseTimeEntity {

	private static final int PARENT_COMMENT = 0;
	private static final String DELETE_COMMENT_CONTENT = "삭제된 댓글입니다.";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false)
	private String contents;

	@Column(nullable = false, columnDefinition = "int default 0")
	private int depth;

	@Column(nullable = false, columnDefinition = "int default 0")
	private int order;

	@Column(insertable = false, updatable = false)
	private Integer userId;

	@Column(insertable = false, updatable = false)
	private Integer meetingDemandId;

	@Column(nullable = false, columnDefinition = "int default 0")
	private int likeCount;

	private Integer parentId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "userId")
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "meetingDemandId", nullable = false)
	private MeetingDemand meetingDemand;

	public static class MeetingDemandCommentListener {

		@PostPersist
		public void setParentId(MeetingDemandComment comment) {
			if (comment.depth == PARENT_COMMENT) {
				comment.parentId = comment.id;
			}
		}
	}

	@Builder
	public MeetingDemandComment(String contents, int depth, int order, User user, MeetingDemand meetingDemand,
		int likeCount, Integer parentId) {
		this.contents = contents;
		this.depth = depth;
		this.order = order;
		this.user = user;
		this.meetingDemand = meetingDemand;
		this.likeCount = likeCount;
		this.parentId = parentId;
	}

	public void updateContents(String contents) {
		this.contents = contents;
	}

	public void deleteParentComment() {
		this.contents = DELETE_COMMENT_CONTENT;
		this.user = null;
		this.userId = null;
	}

	public void validateWriter(Integer userId) {
		if (!isWriter(userId)) {
			throw new ForbiddenException(FORBIDDEN_EXCEPTION.getErrorCode());
		}
	}

	public boolean isWriter(Integer userId) {
		return this.userId != null && this.userId.equals(userId);
	}

	public boolean isParentComment() {
		return this.depth == PARENT_COMMENT;
	}

	public boolean isReplyComment() {
		return !isParentComment();
	}

	public void increaseLikeCount() {
		this.likeCount++;
	}

	public void decreaseLikeCount() {
		if (this.likeCount > 0) {
			this.likeCount--;
		}
	}
}
