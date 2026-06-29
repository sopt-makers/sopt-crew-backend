package org.sopt.makers.crew.main.entity.meetingdemand;

import java.util.List;

import org.hibernate.annotations.Type;
import org.sopt.makers.crew.main.entity.common.BaseTimeEntity;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.vo.MeetingJoinInfo;
import org.sopt.makers.crew.main.entity.meetingdemand.enums.MeetingDemandStatus;
import org.sopt.makers.crew.main.entity.tag.enums.MeetingKeywordType;
import org.sopt.makers.crew.main.entity.user.User;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "meeting_demand")
public class MeetingDemand extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(insertable = false, updatable = false)
	private Integer userId;

	@Column(nullable = false, length = 30)
	private String shortIntro;

	@Column(nullable = false, length = 1000)
	private String expectation;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private MeetingDemandStatus status;

	@Column(insertable = false, updatable = false)
	private Integer meetingId;

	@Column(nullable = false, columnDefinition = "jsonb")
	@Type(JsonBinaryType.class)
	private List<MeetingKeywordType> meetingKeywordTypes;

	@Column(nullable = false, columnDefinition = "jsonb")
	@Type(JsonBinaryType.class)
	private MeetingJoinInfo joinInfo;

	@Column(nullable = false, columnDefinition = "int default 0")
	private int waitCount;

	@Column(nullable = false, columnDefinition = "int default 0")
	private int commentCount;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "userId", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "meetingId")
	private Meeting meeting;

	@Builder
	public MeetingDemand(User user, String shortIntro, String expectation,
		List<MeetingKeywordType> meetingKeywordTypes, MeetingJoinInfo joinInfo) {
		this.user = user;
		this.userId = user.getId();
		this.shortIntro = shortIntro;
		this.expectation = expectation;
		this.status = MeetingDemandStatus.BEFORE_OPEN;
		this.meetingKeywordTypes = meetingKeywordTypes;
		this.joinInfo = joinInfo;
		this.waitCount = 0;
		this.commentCount = 0;
	}

	public void open(Meeting openedMeeting) {
		this.meeting = openedMeeting;
		this.meetingId = openedMeeting.getId();
		this.status = MeetingDemandStatus.OPENED;
	}

	public void increaseWaitCount() {
		this.waitCount++;
	}

	public void decreaseWaitCount() {
		this.waitCount--;
	}

	public void increaseCommentCount() {
		this.commentCount++;
	}

	public void decreaseCommentCount() {
		this.commentCount--;
	}
}
