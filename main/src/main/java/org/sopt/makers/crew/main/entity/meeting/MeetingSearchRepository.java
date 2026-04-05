package org.sopt.makers.crew.main.entity.meeting;

import java.util.List;

import org.sopt.makers.crew.main.global.util.Time;
import org.sopt.makers.crew.main.meeting.v2.dto.query.MeetingV2GetAllMeetingQueryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MeetingSearchRepository {
	/**
	 * 조건에 맞는 모임과 전체 개수를 함께 조회한다.
	 */
	Page<Meeting> findAllByQuery(MeetingV2GetAllMeetingQueryDto queryCommand, Pageable pageable, Time time,
		Integer activeGeneration);

	/**
	 * 조건과 페이지 정보에 맞는 모임 목록만 조회한다.
	 */
	List<Meeting> findMeetingsByQuery(MeetingV2GetAllMeetingQueryDto queryCommand, Pageable pageable, Time time,
		Integer activeGeneration);

	/**
	 * 조건에 맞는 모임의 전체 개수를 조회한다.
	 */
	long countMeetingsByQuery(MeetingV2GetAllMeetingQueryDto queryCommand, Time time, Integer activeGeneration);

	List<Meeting> findRecommendMeetings(List<Integer> meetingIds, Time time);

	Page<Meeting> findAllByQuery(Pageable pageable);
}
