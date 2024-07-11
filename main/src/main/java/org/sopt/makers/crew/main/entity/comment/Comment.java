package org.sopt.makers.crew.main.entity.comment;

import static org.sopt.makers.crew.main.common.response.ErrorStatus.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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

import org.sopt.makers.crew.main.common.exception.ForbiddenException;
import org.sopt.makers.crew.main.entity.post.Post;
import org.sopt.makers.crew.main.entity.user.User;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "comment")
public class Comment {

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
	 * 댓글 깊이
	 */
	@Column(nullable = false, columnDefinition = "int default 0")
	private int depth;

	/**
	 * 댓글 순서
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
	@JoinColumn(name = "userId", nullable = false)
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
	 * 부모 댓글 정보
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parentId")
	private Comment parent;

	/**
	 * 부모 댓글의 고유 식별자
	 */
	@Column(insertable = false, updatable = false)
	private Integer parentId;

	@Builder
	public Comment(String contents, User user, Post post, Comment parent) {
		this.contents = contents;
		this.user = user;
		this.userId = user.getId();
		this.post = post;
		this.parent = parent;
		this.depth = 0;
		this.order = 0;
		this.likeCount = 0;
		this.post.addComment(this);
	}

	public void updateContents(String contents, LocalDateTime updatedDate) {
		this.contents = contents;
		this.updatedDate = updatedDate;
	}

	public void isWriter(Integer userId) {
		boolean isWriter = this.userId.equals(userId);
		if (!isWriter) {
			throw new ForbiddenException(FORBIDDEN_EXCEPTION.getErrorCode());
		}
	}

}
