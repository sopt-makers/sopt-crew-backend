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

@Entity
@Table(name = "apply")
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

	protected Apply() {
	}

	public static ApplyBuilder builder() {
		return new ApplyBuilder();
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

	public Integer getId() {
		return this.id;
	}

	public EnApplyType getType() {
		return this.type;
	}

	public Meeting getMeeting() {
		return this.meeting;
	}

	public Integer getMeetingId() {
		return this.meetingId;
	}

	public User getUser() {
		return this.user;
	}

	public Integer getUserId() {
		return this.userId;
	}

	public String getContent() {
		return this.content;
	}

	public LocalDateTime getAppliedDate() {
		return this.appliedDate;
	}

	public EnApplyStatus getStatus() {
		return this.status;
	}

	public boolean equals(final Object o) {
		if (o == this)
			return true;
		if (!(o instanceof Apply))
			return false;
		final Apply other = (Apply)o;
		if (!other.canEqual((Object)this))
			return false;
		final Object this$id = this.getId();
		final Object other$id = other.getId();
		if (this$id == null ? other$id != null : !this$id.equals(other$id))
			return false;
		final Object this$type = this.getType();
		final Object other$type = other.getType();
		if (this$type == null ? other$type != null : !this$type.equals(other$type))
			return false;
		final Object this$meeting = this.getMeeting();
		final Object other$meeting = other.getMeeting();
		if (this$meeting == null ? other$meeting != null : !this$meeting.equals(other$meeting))
			return false;
		final Object this$meetingId = this.getMeetingId();
		final Object other$meetingId = other.getMeetingId();
		if (this$meetingId == null ? other$meetingId != null : !this$meetingId.equals(other$meetingId))
			return false;
		final Object this$user = this.getUser();
		final Object other$user = other.getUser();
		if (this$user == null ? other$user != null : !this$user.equals(other$user))
			return false;
		final Object this$userId = this.getUserId();
		final Object other$userId = other.getUserId();
		if (this$userId == null ? other$userId != null : !this$userId.equals(other$userId))
			return false;
		final Object this$content = this.getContent();
		final Object other$content = other.getContent();
		if (this$content == null ? other$content != null : !this$content.equals(other$content))
			return false;
		final Object this$appliedDate = this.getAppliedDate();
		final Object other$appliedDate = other.getAppliedDate();
		if (this$appliedDate == null ? other$appliedDate != null : !this$appliedDate.equals(other$appliedDate))
			return false;
		final Object this$status = this.getStatus();
		final Object other$status = other.getStatus();
		if (this$status == null ? other$status != null : !this$status.equals(other$status))
			return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof Apply;
	}

	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $id = this.getId();
		result = result * PRIME + ($id == null ? 43 : $id.hashCode());
		final Object $type = this.getType();
		result = result * PRIME + ($type == null ? 43 : $type.hashCode());
		final Object $meeting = this.getMeeting();
		result = result * PRIME + ($meeting == null ? 43 : $meeting.hashCode());
		final Object $meetingId = this.getMeetingId();
		result = result * PRIME + ($meetingId == null ? 43 : $meetingId.hashCode());
		final Object $user = this.getUser();
		result = result * PRIME + ($user == null ? 43 : $user.hashCode());
		final Object $userId = this.getUserId();
		result = result * PRIME + ($userId == null ? 43 : $userId.hashCode());
		final Object $content = this.getContent();
		result = result * PRIME + ($content == null ? 43 : $content.hashCode());
		final Object $appliedDate = this.getAppliedDate();
		result = result * PRIME + ($appliedDate == null ? 43 : $appliedDate.hashCode());
		final Object $status = this.getStatus();
		result = result * PRIME + ($status == null ? 43 : $status.hashCode());
		return result;
	}

	public static class ApplyBuilder {
		private EnApplyType type;
		private Meeting meeting;
		private Integer meetingId;
		private User user;
		private Integer userId;
		private String content;

		ApplyBuilder() {
		}

		public ApplyBuilder type(EnApplyType type) {
			this.type = type;
			return this;
		}

		public ApplyBuilder meeting(Meeting meeting) {
			this.meeting = meeting;
			return this;
		}

		public ApplyBuilder meetingId(Integer meetingId) {
			this.meetingId = meetingId;
			return this;
		}

		public ApplyBuilder user(User user) {
			this.user = user;
			return this;
		}

		public ApplyBuilder userId(Integer userId) {
			this.userId = userId;
			return this;
		}

		public ApplyBuilder content(String content) {
			this.content = content;
			return this;
		}

		public Apply build() {
			return new Apply(this.type, this.meeting, this.meetingId, this.user, this.userId, this.content);
		}

		public String toString() {
			return "Apply.ApplyBuilder(type=" + this.type + ", meeting=" + this.meeting + ", meetingId="
				+ this.meetingId + ", user=" + this.user + ", userId=" + this.userId + ", content=" + this.content
				+ ")";
		}
	}
}
