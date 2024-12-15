package org.sopt.makers.crew.main.meeting.v2.dto.redis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.sopt.makers.crew.main.entity.meeting.CoLeader;
import org.sopt.makers.crew.main.entity.meeting.CoLeaders;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CoLeadersRedisDto {
	private final Map<Integer, List<CoLeaderRedisDto>> coLeadersMap = new HashMap<>();

	public CoLeadersRedisDto(List<CoLeader> coLeaders) {
		coLeaders
			.forEach(coLeader -> {
				CoLeaderRedisDto coLeaderRedisDto = new CoLeaderRedisDto(coLeader.getUser().getId(),
					coLeader.getUser().getOrgId(),
					coLeader.getUser().getName(), coLeader.getUser().getProfileImage(), coLeader.getMeeting().getId());
				List<CoLeaderRedisDto> coLeaderRedisDtos = coLeadersMap.get(coLeader.getMeeting().getId());
				coLeaderRedisDtos.add(coLeaderRedisDto);
				coLeadersMap.put(coLeader.getMeeting().getId(), coLeaderRedisDtos);
			});
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

