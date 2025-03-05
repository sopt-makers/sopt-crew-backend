package org.sopt.makers.crew.main.external.notification.dto.event;

import java.util.List;

public record FlashCreatedEventDto(
	List<Integer> orgIds,
	Integer meetingId,
	String pushNotificationContent
) {
}
