package org.sopt.makers.crew.main.dining.v2.dto.event;

/** 상태 변경 트랜잭션에서 확정한 수신자와 회식명. */
public record DiningPayerRejectedEvent(Integer meetingId, Integer roundId, Integer requesterUserId,
	String diningTitle) {
}
