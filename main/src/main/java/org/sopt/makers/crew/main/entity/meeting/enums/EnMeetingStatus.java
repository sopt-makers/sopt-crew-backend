package org.sopt.makers.crew.main.entity.meeting.enums;

import java.util.Arrays;
import org.sopt.makers.crew.main.global.exception.BadRequestException;

/** 모임 상태 */
public enum EnMeetingStatus {
  /** 시작 전 */
  BEFORE_START(0),

  /** 지원 가능 */
  APPLY_ABLE(1),

  /** 모집 완료 */
  RECRUITMENT_COMPLETE(2);

  private final int value;

  EnMeetingStatus(int value) {
    this.value = value;
  }

  public static EnMeetingStatus ofValue(int dbData) {
    return Arrays.stream(EnMeetingStatus.values()).filter(v -> v.getValue() == (dbData)).findFirst()
        .orElseThrow(() -> new BadRequestException(
            String.format("EnMeetingStatus 클래스에 value = [%s] 값을 가진 enum 객체가 없습니다.", dbData)));
  }

  public int getValue() {
    return value;
  }
}
