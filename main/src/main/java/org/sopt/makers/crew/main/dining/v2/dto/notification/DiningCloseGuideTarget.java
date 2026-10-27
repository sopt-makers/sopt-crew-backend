package org.sopt.makers.crew.main.dining.v2.dto.notification;

/** 마감 안내 대상: 오늘 회식 중 마감하지 않은 회차가 있는 회식의 개설자 */
public record DiningCloseGuideTarget(Integer meetingId, String diningTitle, Integer leaderUserId) {
}
