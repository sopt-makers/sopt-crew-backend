package org.sopt.makers.crew.main.entity.meeting;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CoLeaders {
	/**
	 * Key : MeetingId
	 * Value : 해당 모임의 공동 모임장 목록
	 *
	 * @implNote : List 내에 있는 CoLeader 객체는 fetch join 으로 다른 객체를 불러오지 않은 상태
	 * @implNote : 해당 자료형을 사용할 때는 'hasCoLeader' 메서드 사용 적극 권장
	 *
	 * */
	private final Map<Integer, List<CoLeader>> coLeadersMap;

	public CoLeaders(List<CoLeader> coLeaders) {
		this.coLeadersMap = coLeaders.stream()
			.collect(Collectors.groupingBy(coLeader -> coLeader.getMeeting().getId()));
	}

	public boolean isCoLeader(Integer meetingId, Integer requestUserId) {
		if (!hasCoLeader(meetingId)) {
			return false;
		}

		return coLeadersMap.get(meetingId).stream()
			.anyMatch(coLeader -> coLeader.getUser().getId().equals(requestUserId));
	}

	public List<CoLeader> getCoLeaders(Integer meetingId) {
		if (!hasCoLeader(meetingId)) {
			return Collections.emptyList();
		}

		return coLeadersMap.get(meetingId);
	}

	private boolean hasCoLeader(Integer meetingId) {
		return coLeadersMap.containsKey(meetingId);
	}

}
