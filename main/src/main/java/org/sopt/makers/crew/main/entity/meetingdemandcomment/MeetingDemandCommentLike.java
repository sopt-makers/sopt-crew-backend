package org.sopt.makers.crew.main.entity.meetingdemandcomment;

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
@Table(name = "meeting_demand_comment_like",
	uniqueConstraints = @UniqueConstraint(
		name = "UQ_meeting_demand_comment_like_comment_user",
		columnNames = {"meetingDemandCommentId", "userId"}
	))
public class MeetingDemandCommentLike extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false)
	private Integer meetingDemandCommentId;

	@Column(nullable = false)
	private Integer userId;

	@Builder
	public MeetingDemandCommentLike(Integer meetingDemandCommentId, Integer userId) {
		this.meetingDemandCommentId = meetingDemandCommentId;
		this.userId = userId;
	}
}
