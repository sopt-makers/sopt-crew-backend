package org.sopt.makers.crew.main.dining.v2.dto.event;

/** 상태 변경 트랜잭션에서 확정한 알림 데이터. 회차 번호는 식별자가 아닌 요청 당시 표시 정보다. */
public record DiningPayerRequestedEvent(Integer meetingId, Integer roundId, Integer requesterUserId,
	Integer leaderUserId, String diningTitle, String requesterName, int roundNumber) {
}
