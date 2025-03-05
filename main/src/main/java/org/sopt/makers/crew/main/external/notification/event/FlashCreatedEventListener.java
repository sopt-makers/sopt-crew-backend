package org.sopt.makers.crew.main.external.notification.event;

import static org.sopt.makers.crew.main.external.notification.PushNotificationEnums.*;

import org.sopt.makers.crew.main.external.notification.PushNotificationService;
import org.sopt.makers.crew.main.external.notification.dto.event.FlashCreatedEventDto;
import org.sopt.makers.crew.main.external.notification.dto.request.PushNotificationRequestDto;
import org.sopt.makers.crew.main.global.config.PushNotificationProperties;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class FlashCreatedEventListener {

	private final PushNotificationService pushNotificationService;
	private final PushNotificationProperties pushNotificationProperties;

	@Async("taskExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleFlashCreatedEvent(FlashCreatedEventDto event) {
		log.info("{}명 FlashCreatedEvent 수신 - meetingId: {}, title: {}", event.orgIds().size(),
			event.meetingId(), event.pushNotificationContent());

		String[] orgIdArray = event.orgIds().stream()
			.map(String::valueOf)
			.toArray(String[]::new);

		String pushNotificationWeblink =
			pushNotificationProperties.getPushWebUrl() + "/detail?id=" + event.meetingId();

		PushNotificationRequestDto pushRequestDto = PushNotificationRequestDto.of(orgIdArray,
			NEW_FLASH_PUSH_NOTIFICATION_TITLE.getValue(),
			event.pushNotificationContent(),
			PUSH_NOTIFICATION_CATEGORY.getValue(), pushNotificationWeblink);

		pushNotificationService.sendPushNotification(pushRequestDto);

		log.info("FlashCreatedEvent 알림 발송 완료");
	}
}
