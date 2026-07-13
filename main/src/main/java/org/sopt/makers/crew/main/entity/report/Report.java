package org.sopt.makers.crew.main.entity.report;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.sopt.makers.crew.main.entity.comment.Comment;
import org.sopt.makers.crew.main.entity.common.BaseTimeEntity;
import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemand;
import org.sopt.makers.crew.main.entity.meetingdemandcomment.MeetingDemandComment;
import org.sopt.makers.crew.main.entity.post.Post;
import org.springframework.data.annotation.CreatedDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "report")
public class Report extends BaseTimeEntity {
	/**
	 * Primary key
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	/**
	 * 작성일
	 */
	@Column(name = "createdDate", nullable = false, columnDefinition = "TIMESTAMP")
	@CreatedDate
	private LocalDateTime createdDate;

	/**
	 * 신고자 id
	 */
	@Column(updatable = false)
	private Integer userId;

	/**
	 * 게시글
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "postId")
	private Post post;

	/**
	 * 게시글 id - 게시글 좋아요가 아닐 경우 null
	 */
	@Column(insertable = false, updatable = false)
	private Integer postId;

	/**
	 * 댓글
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "commentId")
	private Comment comment;

	/**
	 * 댓글 id - 댓글 좋아요가 아닐 경우 null
	 */
	@Column(insertable = false, updatable = false)
	private Integer commentId;

	/**
	 * 모임 수요
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "meetingDemandId")
	private MeetingDemand meetingDemand;

	/**
	 * 모임 수요 id - 모임 수요 신고가 아닐 경우 null
	 */
	@Column(insertable = false, updatable = false)
	private Integer meetingDemandId;

	/**
	 * 모임 수요 댓글
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "meetingDemandCommentId")
	private MeetingDemandComment meetingDemandComment;

	/**
	 * 모임 수요 댓글 id - 모임 수요 댓글 신고가 아닐 경우 null
	 */
	@Column(insertable = false, updatable = false)
	private Integer meetingDemandCommentId;

	@Builder
	public Report(Integer userId, Post post, Integer postId, Comment comment,
		Integer commentId, MeetingDemand meetingDemand, Integer meetingDemandId,
		MeetingDemandComment meetingDemandComment, Integer meetingDemandCommentId) {
		this.userId = userId;
		this.post = post;
		this.postId = postId;
		this.comment = comment;
		this.commentId = commentId;
		this.meetingDemand = meetingDemand;
		this.meetingDemandId = meetingDemandId;
		this.meetingDemandComment = meetingDemandComment;
		this.meetingDemandCommentId = meetingDemandCommentId;
	}
}
