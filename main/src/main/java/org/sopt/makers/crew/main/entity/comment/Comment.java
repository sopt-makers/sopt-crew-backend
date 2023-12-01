package org.sopt.makers.crew.main.entity.comment;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import lombok.*;
import java.util.List;
import org.sopt.makers.crew.main.entity.like.Like;
import org.sopt.makers.crew.main.entity.like.Report;
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
    private int id;

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
    private int userId;

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
    private int postId;

    /**
     * 댓글에 대한 좋아요 목록
     */
    @OneToMany(mappedBy = "comment", cascade = CascadeType.REMOVE)
    private List<Like> likes = new ArrayList<>();

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
    private int parentId;

    /**
     * 자식 댓글 목록
     */
    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
    private List<Comment> children;

    /**
     * 댓글에 대한 신고 목록
     */
    @OneToMany(mappedBy = "comment", cascade = CascadeType.REMOVE)
    private List<Report> reports;

    @Builder
    public Comment(String contents, int depth, int order,
                   User user, int userId, Post post, int postId, int likeCount, Comment parent,
                   int parentId, List<Comment> children, List<Report> reports) {
        this.contents = contents;
        this.depth = depth;
        this.order = order;
        this.user = user;
        this.userId = userId;
        this.post = post;
        this.postId = postId;
        this.likeCount = 0;
        this.parent = parent;
        this.parentId = parentId;
    }

    public void addLike(Like like) {
        likes.add(like);
    }

    public void addChildrenComment(Comment comment) {
        children.add(comment);
    }

    public void addReport(Report report) {
        reports.add(report);
    }
}
