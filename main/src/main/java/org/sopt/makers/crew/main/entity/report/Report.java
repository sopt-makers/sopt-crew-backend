package org.sopt.makers.crew.main.entity.like;

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
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.makers.crew.main.entity.comment.Comment;
import org.sopt.makers.crew.main.entity.post.Post;
import org.sopt.makers.crew.main.entity.user.User;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "like")
@EntityListeners(AuditingEntityListener.class)
public class Report {
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
     * 신고자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    /**
     * 신고자 id
     */
    @Column(insertable = false, updatable = false)
    private int userId;

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
    private int postId;

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
    private int commentId;

    @Builder
    public Report(User user, int userId, Post post, int postId, Comment comment,
                  int commentId) {
        this.user = user;
        this.userId = userId;
        this.post = post;
        this.postId = postId;
        this.comment = comment;
        this.commentId = commentId;
    }
}
