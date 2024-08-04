package org.sopt.makers.crew.main.meeting.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
@Schema(name = "MeetingV2GetMeetingBannerResponseUserDto", description = "모임 배너 유저 Dto")
public class MeetingV2GetMeetingBannerResponseUserDto {
  /** 개설자 crew ID */
  @Schema(description = "모임장 id", example = "1")
  private Integer id;
  /** 개설자 */
  @Schema(description = "모임장 이름", example = "홍길동")
  private String name;
  /** 개설자 playground ID */
  @Schema(description = "모임장 org id", example = "1")
  private Integer orgId;
  /** 프로필 사진 */
  @Schema(description = "모임장 프로필 사진", example = "[url] 형식")
  private String profileImage;
}
