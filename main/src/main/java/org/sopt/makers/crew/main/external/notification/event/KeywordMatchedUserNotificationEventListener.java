package org.sopt.makers.crew.main.external.notification.event;

import org.sopt.makers.crew.main.external.notification.PushNotificationEnums;
import org.sopt.makers.crew.main.external.notification.PushNotificationService;
import org.sopt.makers.crew.main.external.notification.dto.event.KeywordEventDto;
import org.sopt.makers.crew.main.external.notification.dto.request.PushNotificationRequestDto;
import org.sopt.makers.crew.main.global.config.PushNotificationProperties;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class KeywordMatchedUserNotificationEventListener {

	private final PushNotificationService pushNotificationService;
	private final PushNotificationProperties pushNotificationProperties;

	@Async("taskExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleUserKeywordNotificationEvent(KeywordEventDto event) {

		String pushNotificationWeblink =
			pushNotificationProperties.getPushWebUrl() + "/detail?id=" + event.meetingId();

		String[] orgIds = event.dtos().stream()
			.map(m -> String.valueOf(m.user().getId()))
			.toArray(String[]::new);

		pushNotificationService.sendPushNotification(PushNotificationRequestDto.of(
			orgIds,
			String.format(PushNotificationEnums.NEW_INTEREST_KEYWORD_PUSH_NOTIFICATION_TITLE.getValue(),
				event.meetingCategory().getMeetingType()),
			event.pushNotificationContent(),
			PushNotificationEnums.PUSH_NOTIFICATION_CATEGORY.getValue(), pushNotificationWeblink));
	}

}
