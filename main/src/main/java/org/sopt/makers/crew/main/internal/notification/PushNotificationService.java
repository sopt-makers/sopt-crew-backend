package org.sopt.makers.crew.main.internal.notification;

import static org.sopt.makers.crew.main.common.exception.ErrorStatus.*;
import static org.sopt.makers.crew.main.internal.notification.PushNotificationEnums.PUSH_NOTIFICATION_ACTION;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.sopt.makers.crew.main.internal.notification.dto.PushNotificationRequestDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
				PUSH_NOTIFICATION_ACTION.getValue(), UUID.randomUUID().toString(), service, request);
		}catch (Exception e){
			log.error(NOTIFICATION_SERVER_ERROR.getErrorCode(), e);
		}
	}
}
