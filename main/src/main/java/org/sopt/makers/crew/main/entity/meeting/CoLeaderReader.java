package org.sopt.makers.crew.main.entity.meeting;

import java.util.List;

import org.sopt.makers.crew.main.meeting.v2.dto.redis.CoLeadersRedisDto;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CoLeaderReader {
	private final CoLeaderRepository coLeaderRepository;

	@Cacheable(value = "coLeadersCache", key = "#meetingId")
	public CoLeadersRedisDto getCoLeaders(Integer meetingId) {
		List<CoLeader> coLeaders = coLeaderRepository.findAllByMeetingId(meetingId);

		return new CoLeadersRedisDto(coLeaders);
	}
}
