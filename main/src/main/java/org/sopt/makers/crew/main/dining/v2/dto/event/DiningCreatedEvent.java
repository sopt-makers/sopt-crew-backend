package org.sopt.makers.crew.main.dining.v2.dto.event;

import java.util.List;

import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;

/** 회식 등록 트랜잭션에서 확정한 회식 정보. 수신자(현재 기수 참여 파트 회원)는 커밋 후 조회한다. */
public record DiningCreatedEvent(Integer meetingId, String diningTitle, Integer leaderUserId,
	List<MeetingJoinablePart> joinableParts, Integer activeGeneration) {
	public DiningCreatedEvent {
		joinableParts = List.copyOf(joinableParts);
	}
}
