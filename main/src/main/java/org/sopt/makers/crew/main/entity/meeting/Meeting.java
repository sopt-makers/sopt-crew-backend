package org.sopt.makers.crew.main.entity.meeting;

import io.hypersistence.utils.hibernate.type.array.EnumArrayType;
import io.hypersistence.utils.hibernate.type.array.internal.AbstractArrayType;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.sopt.makers.crew.main.entity.apply.Apply;
import org.sopt.makers.crew.main.entity.meeting.converter.MeetingCategoryConverter;
import org.sopt.makers.crew.main.entity.meeting.enums.EnMeetingStatus;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.entity.meeting.vo.ImageUrlVO;
import org.sopt.makers.crew.main.entity.post.Post;
import org.sopt.makers.crew.main.entity.user.User;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "meeting")
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

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
    private Integer userId;

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
    @Column(name = "imageURL", columnDefinition = "jsonb")
    @Type(JsonBinaryType.class)
    //@JdbcTypeCode(SqlTypes.JSON)
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
    private Integer capacity;

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
    private Boolean isMentorNeeded;

    /**
     * 활동 기수만 참여 가능한지 여부
     */
    @Column(name = "canJoinOnlyActiveGeneration", nullable = false)
    private Boolean canJoinOnlyActiveGeneration;

    /**
     * 모임 기수
     */
    @Column(name = "createdGeneration", nullable = false)
    private Integer createdGeneration;

    /**
     * 대상 활동 기수
     */
    @Column(name = "targetActiveGeneration")
    private Integer targetActiveGeneration;

    /**
     * 모임 참여 가능한 파트
     */
    @Type(value = EnumArrayType.class,
            parameters = @Parameter(name = AbstractArrayType.SQL_ARRAY_TYPE,
                    value = "meeting_joinableparts_enum"))
    @Column(name = "joinableParts", columnDefinition = "meeting_joinableparts_enum[]")
    private MeetingJoinablePart[] joinableParts;

    /**
     * 게시글 리스트
     */
    @OneToMany(mappedBy = "meeting", cascade = CascadeType.REMOVE)
    private List<Post> posts;

    @Builder
    public Meeting(User user, Integer userId, List<Apply> appliedInfo, String title, MeetingCategory category,
                   List<ImageUrlVO> imageURL, LocalDateTime startDate, LocalDateTime endDate, Integer capacity,
                   String desc, String processDesc, LocalDateTime mStartDate, LocalDateTime mEndDate,
                   String leaderDesc, String targetDesc, String note, Boolean isMentorNeeded,
                   Boolean canJoinOnlyActiveGeneration, Integer createdGeneration,
                   Integer targetActiveGeneration, MeetingJoinablePart[] joinableParts) {
        this.user = user;
        this.userId = userId;
        this.appliedInfo = appliedInfo != null ? appliedInfo : new ArrayList<>();
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

    public Boolean isHost(Integer userId){
        return this.getUserId().equals(userId);
    }

    /**
     * 모임 모집상태 확인
     *
     * @return 모임 모집상태
     */
    public EnMeetingStatus getMeetingStatus(LocalDateTime now) {
        if (now.isBefore(startDate)) {
            return EnMeetingStatus.BEFORE_RECRUITMENT;
        } else if (now.isBefore(endDate)) {
            return EnMeetingStatus.RECRUITING;
        } else if (now.isBefore(mStartDate)) {
            return EnMeetingStatus.CLOSE_RECRUITMENT;
        } else if (now.isBefore(mEndDate)) {
            return EnMeetingStatus.ACTIVE;
        } else {
            return EnMeetingStatus.ACTIVITY_END;
        }
    }
}