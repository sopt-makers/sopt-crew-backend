package org.sopt.makers.crew.main.external.notification;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.crew.main.external.notification.dto.PushNotificationRequestDto;
import org.sopt.makers.crew.main.external.notification.dto.PushNotificationResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PushNotificationService {

  private static final String ACTION = "send";

  @Value("${push-notification.x-api-key}")
  private String pushNotificationApiKey;

  @Value("${push-notification.service}")
  private String service;

  private final PushNotificationServerClient pushServerClient;

  public void sendPushNotification(PushNotificationRequestDto request) {
    PushNotificationResponseDto response = pushServerClient.sendPushNotification(
        pushNotificationApiKey,
        ACTION,
        UUID.randomUUID().toString(),
        service,
        request
    );

    System.out.println(response.getMessage());
  }
}
