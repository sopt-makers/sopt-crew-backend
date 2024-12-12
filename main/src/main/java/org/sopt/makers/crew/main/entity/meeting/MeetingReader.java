package org.sopt.makers.crew.main.entity.meeting;

import org.sopt.makers.crew.main.meeting.v2.dto.redis.MeetingRedisDto;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingReader {

	private final MeetingRepository meetingRepository;

	@Cacheable(value = "meetingCache", key = "#meetingId")
	public MeetingRedisDto getMeetingById(Integer meetingId) {
		Meeting meeting = meetingRepository.findByIdOrThrow(meetingId);
		return MeetingRedisDto.of(meeting);
	}
}
