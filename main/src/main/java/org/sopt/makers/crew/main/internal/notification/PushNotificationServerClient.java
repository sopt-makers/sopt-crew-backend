package org.sopt.makers.crew.main.internal.notification;

import org.sopt.makers.crew.main.internal.notification.dto.PushNotificationRequestDto;
import org.sopt.makers.crew.main.internal.notification.dto.PushNotificationResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(value = "pushNotification", url = "${push-notification.push-server-url}")
public interface PushNotificationServerClient {

  @PostMapping()
  public PushNotificationResponseDto sendPushNotification(
      @RequestHeader("x-api-key") String pushNotificationApiKey,
      @RequestHeader("action") String action,
      @RequestHeader("transactionId") String transactionId,
      @RequestHeader("service") String service,
      @RequestBody PushNotificationRequestDto request
  );

}
