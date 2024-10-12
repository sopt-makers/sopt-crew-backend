package org.sopt.makers.crew.main.entity.meeting;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JointLeaders {
	/**
	 * Key : MeetingId
	 * Value : 해당 모임의 공동 모임장 목록
	 *
	 * @implNote : List 내에 있는 JointLeader 객체는 fetch join 으로 다른 객체를 불러오지 않은 상태
	 * @implNote : 해당 자료형을 사용할 때는 'hasNotJointLeader' 메서드 사용 적극 권장
	 *
	 * */
	private final Map<Integer, List<JointLeader>> jointLeadersMap;

	public JointLeaders(List<JointLeader> jointLeaders) {
		this.jointLeadersMap = jointLeaders.stream()
			.collect(Collectors.groupingBy(jointLeader -> jointLeader.getMeeting().getId()));
	}

	public boolean isJointLeader(Integer meetingId, Integer requestUserId) {
		if (hasNotJointLeader(meetingId)) {
			return false;
		}

		return jointLeadersMap.get(meetingId).stream()
			.anyMatch(jointLeader -> jointLeader.getUser().getId().equals(requestUserId));
	}

	public List<JointLeader> getJointLeaders(Integer meetingId) {
		if (hasNotJointLeader(meetingId)) {
			return Collections.emptyList();
		}

		return jointLeadersMap.get(meetingId);
	}

	private boolean hasNotJointLeader(Integer meetingId) {
		return !jointLeadersMap.containsKey(meetingId);
	}

}
