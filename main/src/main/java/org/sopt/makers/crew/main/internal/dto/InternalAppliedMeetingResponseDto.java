package org.sopt.makers.crew.main.internal.dto;

import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;

public record InternalAppliedMeetingResponseDto(MeetingCategory meetingCategory, String meetingTitle,
												String meetingDate, Boolean isLeader, String imgUrlÏ) {
}
