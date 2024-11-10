package org.sopt.makers.crew.main.entity.meeting;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
@Transactional
public class MeetingReader {

	private final MeetingRepository meetingRepository;

	@Cacheable(value = "meetingCache", key = "#meetingId")
	public Meeting getMeetingById(Integer meetingId) {
		return meetingRepository.findByIdOrThrow(meetingId);
	}
}
