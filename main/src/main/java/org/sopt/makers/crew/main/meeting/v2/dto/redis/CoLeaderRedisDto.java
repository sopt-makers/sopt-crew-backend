package org.sopt.makers.crew.main.meeting.v2.dto.redis;

import org.sopt.makers.crew.main.entity.meeting.CoLeader;
import org.sopt.makers.crew.main.entity.user.User;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import lombok.Getter;

@Getter
@JsonTypeName("CoLeaderRedisDto")
public class CoLeaderRedisDto {
	private final Integer userId;
	private final Integer orgId;
	private final String userName;
	private final String userProfileImage;
	private final Integer meetingId;

	@JsonCreator
	public CoLeaderRedisDto(
		@JsonProperty("userId") Integer userId,
		@JsonProperty("orgId") Integer orgId,
		@JsonProperty("userName") String userName,
		@JsonProperty("userProfileImage") String userProfileImage,
		@JsonProperty("meetingId") Integer meetingId) {

		this.userId = userId;
		this.orgId = orgId;
		this.userName = userName;
		this.userProfileImage = userProfileImage;
		this.meetingId = meetingId;
	}

	public CoLeader toEntity() {
		User coLeader = new User(userName, orgId, null, userProfileImage, null).withUserIdForRedis(userId);

		return CoLeader.builder()
			.meeting(null)
			.user(coLeader)
			.build();
	}
}

