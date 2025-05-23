package org.sopt.makers.crew.main.external.notification.dto.event;

import java.util.List;

import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;
import org.sopt.makers.crew.main.external.notification.vo.KeywordMatchedUserDto;

public record KeywordEventDto(List<KeywordMatchedUserDto> dtos,
							  Integer meetingId,
							  String pushNotificationContent,
							  MeetingCategory meetingCategory
) {
}
