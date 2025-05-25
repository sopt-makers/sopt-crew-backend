package org.sopt.makers.crew.main.external.notification.event;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NotificationTimeValidatorTest {

	@Test
	@DisplayName("22시 이후, 오전 8시 이전에는 알림을 보내지 않는다. - 알림 안 보내는 시간대")
	void sendNotificationInvalidTime() {
		LocalDateTime now = LocalDateTime.of(LocalDate.now(), LocalTime.of(22, 1));
		LocalDateTime end = LocalDateTime.of(LocalDate.now(), LocalTime.of(7, 59));

		Assertions.assertThat(NotificationTimeValidator.isPublishedTime(now)).isFalse();
		Assertions.assertThat(NotificationTimeValidator.isPublishedTime(end)).isFalse();
	}

	@Test
	@DisplayName("22시 이후, 오전 8시 이전에는 알림을 보내지 않는다. - 알림 보내는 시간대")
	void sendNotificationvalidTime() {
		LocalDateTime now = LocalDateTime.of(LocalDate.now(), LocalTime.of(21, 59));
		LocalDateTime end = LocalDateTime.of(LocalDate.now(), LocalTime.of(8, 1));

		Assertions.assertThat(NotificationTimeValidator.isPublishedTime(now)).isTrue();
		Assertions.assertThat(NotificationTimeValidator.isPublishedTime(end)).isTrue();
	}

}