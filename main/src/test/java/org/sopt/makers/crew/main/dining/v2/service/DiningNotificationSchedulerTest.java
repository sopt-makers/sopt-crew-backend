package org.sopt.makers.crew.main.dining.v2.service;

import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sopt.makers.crew.main.dining.v2.dto.notification.DiningCloseGuideTarget;
import org.sopt.makers.crew.main.dining.v2.dto.notification.DiningReminderTarget;

class DiningNotificationSchedulerTest {

	private final DiningReminderService reminderService = mock(DiningReminderService.class);
	private final DiningNotificationSender sender = mock(DiningNotificationSender.class);
	private final DiningNotificationScheduler scheduler = new DiningNotificationScheduler(reminderService, sender);

	@Test
	@DisplayName("마감 안내는 한 회식의 발송이 실패해도 나머지 회식에 보낸다.")
	void closeGuide_continuesAfterFailure() {
		var failing = new DiningCloseGuideTarget(1, "실패 회식", 10);
		var next = new DiningCloseGuideTarget(2, "다음 회식", 11);
		when(reminderService.findCloseGuideTargets()).thenReturn(List.of(failing, next));
		doThrow(new RuntimeException("push down")).when(sender).sendCloseGuideNotification(failing);

		scheduler.sendCloseGuideNotifications();

		verify(sender).sendCloseGuideNotification(next);
	}

	@Test
	@DisplayName("전날 리마인드는 한 회식의 발송이 실패해도 나머지 회식에 보낸다.")
	void reminder_continuesAfterFailure() {
		var failing = new DiningReminderTarget(1, "실패 회식", List.of(10));
		var next = new DiningReminderTarget(2, "다음 회식", List.of(11));
		when(reminderService.findReminderTargets()).thenReturn(List.of(failing, next));
		doThrow(new RuntimeException("push down")).when(sender).sendReminderNotification(failing);

		scheduler.sendReminderNotifications();

		verify(sender).sendReminderNotification(next);
	}
}
