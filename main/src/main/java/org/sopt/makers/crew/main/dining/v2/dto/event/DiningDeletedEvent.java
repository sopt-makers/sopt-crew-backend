package org.sopt.makers.crew.main.dining.v2.dto.event;

import java.util.List;

/** 회식 삭제 트랜잭션에서 확정한 수신자와 회식명. 커밋 후에는 회식이 없으므로 발송에 필요한 값을 모두 담는다. */
public record DiningDeletedEvent(Integer meetingId, String diningTitle, List<Integer> receiverUserIds) {
	public DiningDeletedEvent {
		receiverUserIds = List.copyOf(receiverUserIds);
	}
}
