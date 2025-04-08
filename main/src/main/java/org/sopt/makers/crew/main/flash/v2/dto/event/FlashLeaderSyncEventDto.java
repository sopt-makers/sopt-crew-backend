package org.sopt.makers.crew.main.flash.v2.dto.event;

public record FlashLeaderSyncEventDto(
	Integer meetingId,
	Integer oldLeaderUserId,
	Integer newLeaderUserId
) {
	public static FlashLeaderSyncEventDto of(Integer meetingId, Integer oldLeaderUserId, Integer newLeaderUserId) {
		return new FlashLeaderSyncEventDto(meetingId, oldLeaderUserId, newLeaderUserId);
	}
}
