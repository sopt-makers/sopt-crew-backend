package org.sopt.makers.crew.main.dining.v2.dto.event;

import java.util.List;

public record DiningRoundCreatedEvent(
	Integer meetingId,
	Integer roundId,
	List<Integer> attendeeUserIds
) {
	public DiningRoundCreatedEvent {
		attendeeUserIds = List.copyOf(attendeeUserIds);
	}
}
