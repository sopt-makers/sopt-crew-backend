package org.sopt.makers.crew.main.entity.meeting.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 모임 상태
 */
@RequiredArgsConstructor
@Getter
public enum EnMeetingStatus {

    BEFORE_RECRUITMENT("모집 전"),

    RECRUITING("모집 중"),

    CLOSE_RECRUITMENT("모집 마감"),

    ACTIVE("활동 중"),

    ACTIVITY_END("활동 종료");

    private final String value;

}
