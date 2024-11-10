package org.sopt.makers.crew.main.entity.meeting;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
@Transactional
public class CoLeaderReader {
	private final CoLeaderRepository coLeaderRepository;

	@Cacheable(value = "coLeadersCache", key = "#meetingId")
	public List<CoLeader> getCoLeaders(Integer meetingId) {
		return coLeaderRepository.findAllByMeetingId(meetingId);
	}
}
