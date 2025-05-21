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
public class UserKeywordNotificationEventListener {

	private final PushNotificationService pushNotificationService;
	private final PushNotificationProperties pushNotificationProperties;

	@Async("keywordThread")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleUserKeywordNotificationEvent(KeywordEventDto event) {

		//pushNotificationProperties.getPushWebUrl() + "/"

		pushNotificationService.sendPushNotification(PushNotificationRequestDto.of(
			event.orgIds().stream()
				.map(String::valueOf)
				.toArray(String[]::new), PushNotificationEnums.NEW_INTEREST_KEYWORD_PUSH_NOTIFICATION_TITLE.getValue(),
			event.pushNotificationContent(),
			PushNotificationEnums.PUSH_NOTIFICATION_ACTION.getValue(),



		));


	}

}
