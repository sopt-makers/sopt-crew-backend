package org.sopt.makers.crew.main.entity.post;

import static org.sopt.makers.crew.main.common.exception.ErrorStatus.FORBIDDEN_EXCEPTION;

import io.hypersistence.utils.hibernate.type.array.StringArrayType;
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
import org.hibernate.annotations.Type;
import org.sopt.makers.crew.main.common.exception.ForbiddenException;
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
    private Integer id;

    /**
     * 게시글 제목
     */
    @Column(nullable = false)
    private String title;

    /**
     * 게시글 내용
     */
    @Column(nullable = false, columnDefinition = "TEXT")
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
    @Column(name = "images", columnDefinition = "text[]")
    @Type(StringArrayType.class)
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
    private Integer userId;

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
    private Integer meetingId;

    /**
     * 게시글에 달린 댓글 수
     */
    @Column(nullable = false, columnDefinition = "int default 0")
    private int commentCount;

    /**
     * 게시글에 대한 좋아요 수
     */
    @Column(nullable = false, columnDefinition = "int default 0")
    private int likeCount;

    @Builder
    public Post(String title, String contents, String[] images, User user, Meeting meeting) {
        this.title = title;
        this.contents = contents;
        this.viewCount = 0;
        this.images = images;
        this.user = user;
        this.userId = user.getId();
        this.meeting = meeting;
        this.meetingId = meeting.getId();
        this.commentCount = 0;
        this.likeCount = 0;
    }

    public void increaseCommentCount() {
        this.commentCount++;
    }

    public void decreaseCommentCount() {
        this.commentCount--;
    }

    public void isWriter(Integer userId) {
        if (!this.userId.equals(userId)) {
            throw new ForbiddenException(FORBIDDEN_EXCEPTION.getErrorCode());
        }
    }

    public void updatePost(String title, String contents, String[] images) {
        this.title = title;
        this.contents = contents;
        this.images = images;
    }
}