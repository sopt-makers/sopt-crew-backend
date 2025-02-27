package org.sopt.makers.crew.main.external.notification;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.util.UUID;

import org.sopt.makers.crew.main.external.notification.dto.PushNotificationRequestDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PushNotificationService {

	@Value("${push-notification.x-api-key}")
	private String pushNotificationApiKey;

	@Value("${push-notification.service}")
	private String service;

	private final PushNotificationServerClient pushServerClient;

	public void sendPushNotification(PushNotificationRequestDto request) {
		try {
			pushServerClient.sendPushNotification(pushNotificationApiKey,
				PushNotificationEnums.PUSH_NOTIFICATION_ACTION.getValue(), UUID.randomUUID().toString(), service,
				request);
		} catch (Exception e) {
			log.error(NOTIFICATION_SERVER_ERROR.getErrorCode(), e);
		}
	}
}
