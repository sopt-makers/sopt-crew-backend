package org.sopt.makers.crew.main.entity.meeting;

import io.hypersistence.utils.hibernate.type.array.EnumArrayType;
import io.hypersistence.utils.hibernate.type.array.internal.AbstractArrayType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;
import org.sopt.makers.crew.main.entity.apply.Apply;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategoryConverter;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.entity.meeting.vo.ImageUrlVO;
import org.sopt.makers.crew.main.entity.post.Post;
import org.sopt.makers.crew.main.entity.user.User;
import org.springframework.data.annotation.CreatedDate;


import java.util.List;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "meeting")
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /**
     * 개설한 유저
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    /**
     * 유저 id
     */
    @Column(insertable = false, updatable = false)
    private int userId;

    /**
     * 지원 or 초대 정보
     */
    @OneToMany(mappedBy = "meeting", cascade = CascadeType.REMOVE)
    private List<Apply> appliedInfo;

    /**
     * 모임 제목
     */
    @Column(nullable = false)
    private String title;

    /**
     * 모임 카테고리
     */
    @Column(name = "category", nullable = false)
    @Convert(converter = MeetingCategoryConverter.class)
    private MeetingCategory category;

    /**
     * 이미지
     */
    @Column(name = "imageURL")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<ImageUrlVO> imageURL;

    /**
     * 모집 시작 기간
     */
    @Column(name = "startDate", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime startDate;

    /**
     * 모집 마감 기간
     */
    @Column(name = "endDate", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime endDate;

    /**
     * 모집 인원
     */
    @Column(name = "capacity", nullable = false)
    private int capacity;

    /**
     * 모임 소개
     */
    @Column(name = "desc", nullable = false)
    private String desc;

    /**
     * 진행방식 소개
     */
    @Column(name = "processDesc", nullable = false)
    private String processDesc;

    /**
     * 모임 시작 기간
     */
    @Column(name = "mStartDate", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime mStartDate;

    /**
     * 모임 마감 기간
     */
    @Column(name = "mEndDate", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime mEndDate;

    /**
     * 개설자 소개
     */
    @Column(name = "leaderDesc", nullable = false)
    private String leaderDesc;

    /**
     * 모집 대상
     */
    @Column(name = "targetDesc", nullable = false)
    private String targetDesc;

    /**
     * 유의 사항
     */
    @Column(name = "note")
    private String note;

    /**
     * 멘토 필요 여부
     */
    @Column(name = "isMentorNeeded", nullable = false)
    private boolean isMentorNeeded;

    /**
     * 활동 기수만 참여 가능한지 여부
     */
    @Column(name = "canJoinOnlyActiveGeneration", nullable = false)
    private boolean canJoinOnlyActiveGeneration;

    /**
     * 모임 기수
     */
    @Column(name = "createdGeneration", nullable = false)
    private int createdGeneration;

    /**
     * 대상 활동 기수
     */
    @Column(name = "targetActiveGeneration")
    private int targetActiveGeneration;

    /**
     * 모임 참여 가능한 파트
     */
    @Type(
            value = EnumArrayType.class,
            parameters = @Parameter(
                    name = AbstractArrayType.SQL_ARRAY_TYPE,
                    value = "meeting_joinableparts_enum"
            )
    )
    @Column(
            name = "joinableParts",
            columnDefinition = "meeting_joinableparts_enum[]"
    )
    private MeetingJoinablePart[] joinableParts;

    /**
     * 게시글 리스트
     */
    @OneToMany(mappedBy = "meeting", cascade = CascadeType.REMOVE)
    private List<Post> posts;

    @Builder
    public Meeting(User user, int userId, String title, MeetingCategory category,
                   List<ImageUrlVO> imageURL, LocalDateTime startDate, LocalDateTime endDate, int capacity, String desc,
                   String processDesc, LocalDateTime mStartDate, LocalDateTime mEndDate, String leaderDesc,
                   String targetDesc,
                   String note, boolean isMentorNeeded, boolean canJoinOnlyActiveGeneration, int createdGeneration,
                   Integer targetActiveGeneration, MeetingJoinablePart[] joinableParts) {
        this.user = user;
        this.userId = userId;
        this.title = title;
        this.category = category;
        this.imageURL = imageURL;
        this.startDate = startDate;
        this.endDate = endDate;
        this.capacity = capacity;
        this.desc = desc;
        this.processDesc = processDesc;
        this.mStartDate = mStartDate;
        this.mEndDate = mEndDate;
        this.leaderDesc = leaderDesc;
        this.targetDesc = targetDesc;
        this.note = note;
        this.isMentorNeeded = isMentorNeeded;
        this.canJoinOnlyActiveGeneration = canJoinOnlyActiveGeneration;
        this.createdGeneration = createdGeneration;
        this.targetActiveGeneration = targetActiveGeneration;
        this.joinableParts = joinableParts;
    }

    public void addApply(Apply apply) {
        appliedInfo.add(apply);
    }

    public void addPost(Post post) {
        posts.add(post);
    }
}
