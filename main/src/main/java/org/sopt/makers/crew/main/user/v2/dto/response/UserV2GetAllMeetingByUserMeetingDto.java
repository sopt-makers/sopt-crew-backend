package org.sopt.makers.crew.main.user.v2.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class UserV2GetAllMeetingByUserMeetingDto {

  private int id;
  private String title;
  private String contents;
  private String imageUrl;
  private String category;
}
