package org.sopt.makers.crew.main.internal.notification;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum PushNotificationEnums {
  PUSH_NOTIFICATION_ACTION("send"),

  PUSH_NOTIFICATION_CATEGORY("NEWS"),

  NEW_POST_PUSH_NOTIFICATION_TITLE("✏️내 모임에 새로운 글이 업로드됐어요.");

  private final String value;
}
