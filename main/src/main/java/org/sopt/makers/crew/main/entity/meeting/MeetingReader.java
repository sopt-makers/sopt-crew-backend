package org.sopt.makers.crew.main.entity.meeting;

import org.sopt.makers.crew.main.meeting.v2.dto.redis.MeetingCacheDto;
import org.sopt.makers.crew.main.global.annotation.NoExecutionLogging;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingReader {

	private final MeetingRepository meetingRepository;

	// 모임 신청 결과 로그(ApplyResultLoggingAop)에서도 신청마다 호출하므로 실행 로깅에서 제외한다
	@NoExecutionLogging
	@Cacheable(value = "meetingCache", key = "#meetingId")
	public MeetingCacheDto getMeetingById(Integer meetingId) {
		Meeting meeting = meetingRepository.findByIdOrThrow(meetingId);
		return MeetingCacheDto.from(meeting);
	}
}
