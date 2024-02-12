package org.sopt.makers.crew.main.meeting.v2.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class MeetingV2GetMeetingBannerResponseUserDto {
  /** 개설자 crew ID */
  private Integer id;
  /** 개설자 */
  private String name;
  /** 개설자 playground ID */
  private Integer orgId;
  /** 프로필 사진 */
  private String profileImage;
}
