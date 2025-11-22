package org.sopt.makers.crew.main.internal.dto;

import java.time.LocalDateTime;

public record UserAppliedMeetingDto(String meetingCategory, String meetingTitle,
									LocalDateTime mStartTime, LocalDateTime mEndTime, Boolean isLeader, String imgUrl) {

	public static UserAppliedMeetingDto of(
		String meetingCategory, String meetingTitle,
		LocalDateTime mStartTime, LocalDateTime mEndTime, Boolean isLeader, String imgUrl
	) {
		return new UserAppliedMeetingDto(
			meetingCategory, meetingTitle, mStartTime, mEndTime, isLeader, imgUrl
		);
	}
}
