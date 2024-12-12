package org.sopt.makers.crew.main.meeting.v2.dto.redis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.sopt.makers.crew.main.entity.meeting.CoLeader;
import org.sopt.makers.crew.main.entity.meeting.CoLeaders;


import com.fasterxml.jackson.annotation.JsonAutoDetect;

import lombok.Getter;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Getter
public class CoLeadersRedisDto {
	private final Map<Integer, List<CoLeaderRedisDto>> coLeadersMap;

	public CoLeadersRedisDto() {
		this.coLeadersMap = new HashMap<>(); // 기본 값으로 설정하거나 필요시 초기화
	}

	public CoLeadersRedisDto(List<CoLeader> coLeaders) {
		this.coLeadersMap = coLeaders.stream()
			.map(coLeader -> new CoLeaderRedisDto(coLeader.getUser().getId(), coLeader.getUser().getOrgId(),
				coLeader.getUser().getName(), coLeader.getUser().getProfileImage(), coLeader.getMeeting().getId()))
			.collect(Collectors.groupingBy(CoLeaderRedisDto::getMeetingId));
	}

	public CoLeaders toEntity() {
		Map<Integer, List<CoLeader>> coLeadersEntityMap = coLeadersMap.entrySet().stream()
			.collect(Collectors.toMap(
				Map.Entry::getKey,
				entry -> entry.getValue().stream()
					.map(CoLeaderRedisDto::toEntity) // CoLeaderRedisDto -> CoLeader 변환
					.toList()
			));

		return new CoLeaders(coLeadersEntityMap);
	}

}

