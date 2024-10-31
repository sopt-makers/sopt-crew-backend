package org.sopt.makers.crew.main.entity.meeting;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.NOT_FOUND_MEETING;

import java.util.List;
import java.util.Optional;

import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MeetingRepository extends JpaRepository<Meeting, Integer>, MeetingSearchRepository {

	List<Meeting> findAllByUserId(Integer userId);

	/**
	 * @implSpec : 특정 유저가 모임장이거나 공동모임장인 모임을 최근에 만들어진 순으로 조회한다.
	 * **/
	@Query("SELECT m "
		+ "FROM Meeting m "
		+ "JOIN fetch m.user "
		+ "WHERE m.user.id =:userId "
		+ "OR m.id IN (:coLeaderMeetingIds)"
		+ "ORDER BY m.id DESC ")
	List<Meeting> findAllByUserIdOrIdInWithUser(Integer userId, List<Integer> coLeaderMeetingIds);

	default Meeting findByIdOrThrow(Integer meetingId) {
		return findById(meetingId)
			.orElseThrow(() -> new BadRequestException(NOT_FOUND_MEETING.getErrorCode()));
	}

	default Meeting findByIdWithUserOrThrow(Integer meetingId) {
		return findByIdWithUser(meetingId)
			.orElseThrow(() -> new BadRequestException(NOT_FOUND_MEETING.getErrorCode()));
	}

	@Query("SELECT m "
		+ "FROM Meeting m "
		+ "JOIN fetch m.user "
		+ "WHERE m.id =:meetingId ")
	Optional<Meeting> findByIdWithUser(Integer meetingId);

	@Query("SELECT m FROM Meeting m JOIN FETCH m.user ORDER BY m.id DESC LIMIT 20")
	List<Meeting> findTop20ByOrderByIdDesc();
}