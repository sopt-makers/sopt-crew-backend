package org.sopt.makers.crew.main.external.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class PushNotificationRequestDto {

  private String[] userIds;

  private String title;

  private String content;

  private String category;

  private String webLink;

}
