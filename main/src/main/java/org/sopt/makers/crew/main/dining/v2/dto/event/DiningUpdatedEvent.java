package org.sopt.makers.crew.main.dining.v2.dto.event;

import java.util.List;

/** 회식 수정 트랜잭션에서 확정한 수신자와 회식명. */
public record DiningUpdatedEvent(Integer meetingId, String diningTitle, List<Integer> receiverUserIds) {
	public DiningUpdatedEvent {
		receiverUserIds = List.copyOf(receiverUserIds);
	}
}
