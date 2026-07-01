package org.sopt.makers.crew.main.entity.meetingdemand;

import org.sopt.makers.crew.main.entity.common.BaseTimeEntity;
import org.sopt.makers.crew.main.entity.meetingdemand.vo.MeetingDemandAnonymousProfile;

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
@Table(name = "meeting_demand_comment_profile",
	uniqueConstraints = @UniqueConstraint(
		name = "UQ_meeting_demand_comment_profile_demand_user",
		columnNames = {"meetingDemandId", "userId"}
	))
public class MeetingDemandCommentProfile extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false)
	private Integer meetingDemandId;

	@Column(nullable = false)
	private Integer userId;

	@Column(nullable = false, length = 30)
	private String anonymousNickname;

	@Column(nullable = false)
	private Integer anonymousImageNumber;

	@Builder
	public MeetingDemandCommentProfile(Integer meetingDemandId, Integer userId) {
		this.meetingDemandId = meetingDemandId;
		this.userId = userId;
		this.anonymousNickname = MeetingDemandAnonymousProfile.generateNickname();
		this.anonymousImageNumber = MeetingDemandAnonymousProfile.generateImageNumber();
	}

	public String getAnonymousImageUrl() {
		return MeetingDemandAnonymousProfile.getImageUrl(anonymousImageNumber);
	}
}
