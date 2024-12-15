package org.sopt.makers.crew.main.entity.meeting;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.sopt.makers.crew.main.global.exception.BadRequestException;

public class CoLeaders {
	/**
	 * Key : MeetingId
	 * Value : 해당 모임의 공동 모임장 목록
	 *
	 * @implNote : List 내에 있는 CoLeader 객체는 fetch join 으로 다른 객체를 불러오지 않은 상태
	 * @implNote : 해당 자료형을 사용할 때는 'isCoLeaderPresent' 메서드 사용 적극 권장
	 *
	 * */
	private final Map<Integer, List<CoLeader>> coLeadersMap;

	public CoLeaders(List<CoLeader> coLeaders) {
		this.coLeadersMap = coLeaders.stream()
			.collect(Collectors.groupingBy(coLeader -> coLeader.getMeeting().getId()));
	}

	public CoLeaders(Map<Integer, List<CoLeader>> coLeadersMap) {
		this.coLeadersMap = coLeadersMap;
	}

	public void validateCoLeader(Integer meetingId, Integer requestUserId) {
		if (isCoLeader(meetingId, requestUserId)) {
			throw new BadRequestException(CO_LEADER_CANNOT_APPLY.getErrorCode());
		}
	}

	public boolean isCoLeader(Integer meetingId, Integer requestUserId) {
		if (!isCoLeaderPresent(meetingId)) {
			return false;
		}

		return coLeadersMap.get(meetingId).stream()
			.anyMatch(coLeader -> coLeader.getUser().getId().equals(requestUserId));
	}

	public List<CoLeader> getCoLeaders(Integer meetingId) {
		if (!isCoLeaderPresent(meetingId)) {
			return Collections.emptyList();
		}

		return Collections.unmodifiableList(coLeadersMap.get(meetingId));
	}

	private boolean isCoLeaderPresent(Integer meetingId) {
		return coLeadersMap.containsKey(meetingId);
	}

}
