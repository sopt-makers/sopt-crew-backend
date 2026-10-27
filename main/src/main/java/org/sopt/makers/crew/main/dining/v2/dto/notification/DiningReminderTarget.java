package org.sopt.makers.crew.main.dining.v2.dto.notification;

import java.util.List;

/** 전날 리마인드 대상: 내일 회식의 전체 회차 참석자 (개설자 포함, 중복 제거) */
public record DiningReminderTarget(Integer meetingId, String diningTitle, List<Integer> receiverUserIds) {
	public DiningReminderTarget {
		receiverUserIds = List.copyOf(receiverUserIds);
	}
}
