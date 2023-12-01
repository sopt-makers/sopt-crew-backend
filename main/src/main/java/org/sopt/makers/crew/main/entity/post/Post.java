package org.sopt.makers.crew.main.entity.post;

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
import org.hibernate.annotations.JdbcTypeCode;


import java.util.List;
import org.hibernate.type.SqlTypes;
import org.sopt.makers.crew.main.entity.comment.Comment;
import org.sopt.makers.crew.main.entity.like.Like;
import org.sopt.makers.crew.main.entity.like.Report;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.user.User;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "post")
public class Post {

    /**
     * 게시글의 고유 식별자
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /**
     * 게시글 제목
     */
    @Column(nullable = false)
    private String title;

    /**
     * 게시글 내용
     */
    @Column(nullable = false)
    private String contents;

    /**
     * 게시글 작성일
     */
    @Column(name = "createdDate", nullable = false, columnDefinition = "TIMESTAMP")
    @CreatedDate
    private LocalDateTime createdDate;

    /**
     * 게시글 수정일
     */
    @Column(name = "updatedDate", nullable = false, columnDefinition = "TIMESTAMP")
    @LastModifiedDate
    private LocalDateTime updatedDate;

    /**
     * 조회수
     */
    @Column(nullable = false, columnDefinition = "int default 0")
    private int viewCount;

    /**
     * 이미지 리스트
     */
    @Column(name = "images")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private String[] images;

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
     * 게시글이 속한 미팅 정보
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meetingId", nullable = false)
    private Meeting meeting;

    /**
     * 게시글이 속한 미팅의 고유 식별자
     */
    @Column(insertable = false, updatable = false)
    private int meetingId;

    /**
     * 게시글에 달린 댓글 목록
     */
    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE)
    private List<Comment> comments = new ArrayList<>();

    /**
     * 게시글에 달린 댓글 수
     */
    @Column(nullable = false, columnDefinition = "int default 0")
    private int commentCount;

    /**
     * 게시글에 대한 좋아요 목록
     */
    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE)
    private List<Like> likes = new ArrayList<>();

    /**
     * 게시글에 대한 좋아요 수
     */
    @Column(nullable = false, columnDefinition = "int default 0")
    private int likeCount;

    /**
     * 게시글에 대한 신고 목록
     */
    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE)
    private List<Report> reports;

    @Builder
    public Post(String title, String contents,
                String[] images, User user, int userId, Meeting meeting, int meetingId) {
        this.title = title;
        this.contents = contents;
        this.viewCount = 0;
        this.images = images;
        this.user = user;
        this.userId = userId;
        this.meeting = meeting;
        this.meetingId = meetingId;
        this.commentCount = 0;
        this.likeCount = 0;
    }

    public void addLike(Like like) {
        likes.add(like);
    }

    public void addComment(Comment comment) {
        comments.add(comment);
    }

    public void addReport(Report report) {
        reports.add(report);
    }
}
