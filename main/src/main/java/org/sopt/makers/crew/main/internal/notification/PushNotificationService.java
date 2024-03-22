package org.sopt.makers.crew.main.internal.notification;

import static org.sopt.makers.crew.main.internal.notification.PushNotificationEnums.PUSH_NOTIFICATION_ACTION;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.crew.main.internal.notification.dto.PushNotificationRequestDto;
import org.sopt.makers.crew.main.internal.notification.dto.PushNotificationResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PushNotificationService {

  @Value("${push-notification.x-api-key}")
  private String pushNotificationApiKey;

  @Value("${push-notification.service}")
  private String service;

  private final PushNotificationServerClient pushServerClient;

  public void sendPushNotification(PushNotificationRequestDto request) {
    PushNotificationResponseDto response =
        pushServerClient.sendPushNotification(pushNotificationApiKey,
            PUSH_NOTIFICATION_ACTION.getValue(), UUID.randomUUID().toString(), service, request);
  }
}
