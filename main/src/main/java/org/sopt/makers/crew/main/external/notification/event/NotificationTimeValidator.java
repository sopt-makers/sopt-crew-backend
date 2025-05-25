package org.sopt.makers.crew.main.external.notification.event;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class NotificationTimeValidator {

	public static boolean isPublishedTime(LocalDateTime now) {
		LocalTime time = now.toLocalTime();
		return !(time.isAfter(LocalTime.of(22, 0)) || time.isBefore(LocalTime.of(8, 0)));
	}

}
