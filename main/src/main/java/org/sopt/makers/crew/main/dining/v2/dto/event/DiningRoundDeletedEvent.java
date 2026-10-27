package org.sopt.makers.crew.main.dining.v2.dto.event;

import java.util.List;

/** 회차 삭제 트랜잭션에서 확정한 수신자, 회식명과 삭제 당시 회차 번호. */
public record DiningRoundDeletedEvent(Integer meetingId, String diningTitle, int roundNumber,
	List<Integer> receiverUserIds) {
	public DiningRoundDeletedEvent {
		receiverUserIds = List.copyOf(receiverUserIds);
	}
}
