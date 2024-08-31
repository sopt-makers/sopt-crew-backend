package org.sopt.makers.crew.main.entity.comment;

import static org.sopt.makers.crew.main.common.exception.ErrorStatus.*;

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

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.sopt.makers.crew.main.common.exception.ForbiddenException;
import org.sopt.makers.crew.main.entity.post.Post;
import org.sopt.makers.crew.main.entity.user.User;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners({AuditingEntityListener.class, Comment.CommentListener.class})
@Table(name = "comment")
public class Comment {

	private static final int PARENT_COMMENT = 0;
	private static final String DELETE_COMMENT_CONTENT = "삭제된 댓글입니다.";

	/**
	 * 댓글의 고유 식별자
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	/**
	 * 댓글 내용
	 */
	@Column(nullable = false)
	private String contents;

	/**
	 * 댓글/대댓글 구분자 (0 = 댓글, 1 = 대댓글)
	 */
	@Column(nullable = false, columnDefinition = "int default 0")
	private int depth;

	/**
	 * 댓글 순서 (댓글일 경우 0, 대댓글은 1부터 시작)
	 */
	@Column(nullable = false, columnDefinition = "int default 0")
	private int order;

	/**
	 * 작성일
	 */
	@Column(name = "createdDate", nullable = false, columnDefinition = "TIMESTAMP")
	@CreatedDate
	private LocalDateTime createdDate;

	/**
	 * 수정일
	 */
	@Column(name = "updatedDate", nullable = false, columnDefinition = "TIMESTAMP")
	@LastModifiedDate
	private LocalDateTime updatedDate;

	/**
	 * 작성자 정보
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "userId")
	private User user;

	/**
	 * 작성자의 고유 식별자
	 */
	@Column(insertable = false, updatable = false)
	private Integer userId;

	/**
	 * 댓글이 속한 게시글 정보
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "postId", nullable = false)
	private Post post;

	/**
	 * 댓글이 속한 게시글의 고유 식별자
	 */
	@Column(insertable = false, updatable = false)
	private Integer postId;

	/**
	 * 댓글에 대한 좋아요 수
	 */
	@Column(nullable = false, columnDefinition = "int default 0")
	private int likeCount;

	/**
	 * 부모 댓글의 고유 식별자
	 */
	private Integer parentId;

	public static class CommentListener {

		@PostPersist
		public void setParentId(Comment comment) {
			if (comment.depth == PARENT_COMMENT) { // 댓글일 경우
				comment.parentId = comment.id;
			}
		}
	}

	@Builder
	public Comment(String contents, int depth, int order, User user, Integer userId, Post post,
		Integer postId,
		int likeCount, Integer parentId) {
		this.contents = contents;
		this.depth = depth;
		this.order = order;
		this.user = user;
		this.userId = userId;
		this.post = post;
		this.postId = postId;
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
		if (this.userId == null || !this.userId.equals(userId)) {
			return false;
		}
		return true;
	}

	public boolean isParentComment() {
		return this.depth == PARENT_COMMENT;
	}

	public void increaseLikeCount() {
		this.likeCount++;
	}

	public void decreaseLikeCount() {
		this.likeCount--;
	}
}
