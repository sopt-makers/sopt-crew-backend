package org.sopt.makers.crew.main.entity.meetingdemand;

import java.time.LocalDateTime;

import org.sopt.makers.crew.main.entity.common.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "meeting_demand_opened_notification",
	uniqueConstraints = @UniqueConstraint(
		name = "UQ_meeting_demand_opened_notification_meeting",
		columnNames = {"meetingId"}
	))
public class MeetingDemandOpenedNotification extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false)
	private Integer meetingId;

	private LocalDateTime sentAt;

	@Builder
	public MeetingDemandOpenedNotification(Integer meetingId) {
		this.meetingId = meetingId;
	}

	public boolean isSent() {
		return sentAt != null;
	}

	public void markSent(LocalDateTime sentAt) {
		this.sentAt = sentAt;
	}
}
