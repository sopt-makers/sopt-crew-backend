package org.sopt.makers.crew.main.external.notification.dto.event;

import java.util.List;

import org.sopt.makers.crew.main.entity.tag.enums.MeetingKeywordType;

public record KeywordEventDto(List<Integer> orgIds,
							  Integer meetingId,
							  String pushNotificationContent,
							  List<MeetingKeywordType> keywordTypes,
							  boolean isFlash
) {
}
