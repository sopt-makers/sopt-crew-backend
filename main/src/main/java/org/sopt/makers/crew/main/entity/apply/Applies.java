package org.sopt.makers.crew.main.entity.apply;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Applies {

	/**
	 * Key : MeetingId
	 * Value : 해당 모임에 신청한 신청정보들
	 *
	 * @note: List 내에 있는 Apply 객체는 fetch join 으로 다른 객체를 불러오지 않은 상태
	 *
	 * */
	private final Map<Integer, List<Apply>> appliesMap;

	public Applies(List<Apply> applies) {
		this.appliesMap = applies.stream()
			.collect(Collectors.groupingBy(Apply::getMeetingId));
	}

	public int getAppliedCount(Integer meetingId){
		List<Apply> applies = appliesMap.get(meetingId);

		if(applies == null){
			return 0;
		}
		return applies.size();
	}
}
