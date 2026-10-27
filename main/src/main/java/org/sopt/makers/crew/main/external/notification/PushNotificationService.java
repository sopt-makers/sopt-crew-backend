package org.sopt.makers.crew.main.external.notification;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.util.UUID;

import org.sopt.makers.crew.main.external.notification.dto.request.PushNotificationRequestDto;
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

	/**
	 * push 서버 호출 실패는 호출부 흐름을 막지 않도록 로그만 남긴다.
	 * @return 발송 요청 성공 여부 (실패 집계가 필요한 호출부에서 사용, 기존 호출부는 무시해도 된다)
	 */
	public boolean sendPushNotification(PushNotificationRequestDto request) {
		try {
			pushServerClient.sendPushNotification(pushNotificationApiKey,
				PushNotificationEnums.PUSH_NOTIFICATION_ACTION.getValue(), UUID.randomUUID().toString(), service,
				request);
			return true;
		} catch (Exception e) {
			log.error(NOTIFICATION_SERVER_ERROR.getErrorCode(), e);
			return false;
		}
	}
}
