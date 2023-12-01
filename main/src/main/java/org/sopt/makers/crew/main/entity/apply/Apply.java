package org.sopt.makers.crew.main.entity.apply;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
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
import org.sopt.makers.crew.main.entity.apply.enums.ApplyStatusConverter;
import org.sopt.makers.crew.main.entity.apply.enums.ApplyTypeConverter;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyStatus;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyType;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.user.User;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "apply")
public class Apply {
    /**
     * Primary Key
     */
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private int id;

    /**
     * 지원 타입
     */
    @Column(name = "type", nullable = false, columnDefinition = "integer default 0")
    @Convert(converter = ApplyTypeConverter.class)
    private EnApplyType type;

    /**
     * 지원한 모임
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meetingId", nullable = false)
    private Meeting meeting;

    /**
     * 지원한 모임 ID
     */
    @Column(insertable = false, updatable = false)
    private int meetingId;

    /**
     * 지원자
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    /**
     * 지원자 ID
     */
    @Column(insertable = false, updatable = false)
    private int userId;

    /**
     * 지원 동기
     */
    @Column(name = "content", nullable = false)
    private String content;

    /**
     * 지원한 날짜
     */
    @Column(name = "appliedDate", nullable = false, columnDefinition = "TIMESTAMP")
    @CreatedDate
    private LocalDateTime appliedDate;

    /**
     * 지원 상태
     */
    @Column(name = "status", nullable = false, columnDefinition = "integer default 0")
    @Convert(converter = ApplyStatusConverter.class)
    private EnApplyStatus status;

    @Builder
    public Apply(EnApplyType type, Meeting meeting, int meetingId, User user, int userId,
                 String content, EnApplyStatus status) {
        this.type = type;
        this.meeting = meeting;
        this.meetingId = meetingId;
        this.user = user;
        this.userId = userId;
        this.content = content;
        this.appliedDate = LocalDateTime.now();
        this.status = status;
    }
}
