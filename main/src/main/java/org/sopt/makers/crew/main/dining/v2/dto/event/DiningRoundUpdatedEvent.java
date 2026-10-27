package org.sopt.makers.crew.main.dining.v2.dto.event;

import java.util.List;

/** 회차 수정 트랜잭션에서 확정한 수신자, 회식명과 현재 회차 번호. */
public record DiningRoundUpdatedEvent(Integer meetingId, String diningTitle, int roundNumber,
	List<Integer> receiverUserIds) {
	public DiningRoundUpdatedEvent {
		receiverUserIds = List.copyOf(receiverUserIds);
	}
}
