package org.sopt.makers.crew.main.entity.apply;

import static jakarta.persistence.GenerationType.*;
import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.time.LocalDateTime;

import org.sopt.makers.crew.main.entity.apply.enums.ApplyStatusConverter;
import org.sopt.makers.crew.main.entity.apply.enums.ApplyTypeConverter;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyStatus;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyType;
import org.sopt.makers.crew.main.entity.common.BaseTimeEntity;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.springframework.data.annotation.CreatedDate;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "apply")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Apply extends BaseTimeEntity {

	/**
	 * Primary Key
	 */
	@Id
	@GeneratedValue(strategy = IDENTITY)
	private Integer id;

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
	private Integer meetingId;

	/**
	 * 지원자
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "userId", nullable = false)
	private User user;

	/**
	 * 지원자 ID
	 */
	@Column(insertable = false, updatable = false)
	private Integer userId;

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

	public Apply(EnApplyType type, Meeting meeting, Integer meetingId, User user, Integer userId,
		String content) {
		this.type = type;
		this.meeting = meeting;
		this.meetingId = meetingId;
		this.user = user;
		this.userId = userId;
		this.content = content;
		this.appliedDate = LocalDateTime.now();
		this.status = EnApplyStatus.WAITING;
	}

	public void updateApplyStatus(EnApplyStatus status) {
		this.status = status;
	}

	public void validateDuplicateUpdateApplyStatus(EnApplyStatus updatedApplyStatus) {
		if (updatedApplyStatus.equals(this.getStatus())) {
			throw new BadRequestException(ALREADY_PROCESSED_APPLY.getErrorCode());
		}
	}

	public String getContent(Integer requestUserId) {
		if (!this.userId.equals(requestUserId)) {
			return "";
		}

		return this.content;
	}
}
