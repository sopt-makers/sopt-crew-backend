package org.sopt.makers.crew.main.dining.v2.dto.event;

public record DiningRoundCreatedEvent(
	Integer meetingId,
	Integer roundId
) {
}
