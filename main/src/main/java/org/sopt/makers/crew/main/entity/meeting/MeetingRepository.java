package org.sopt.makers.crew.main.entity.meeting;

import static org.sopt.makers.crew.main.common.exception.ErrorStatus.NOT_FOUND_MEETING;

import java.util.List;

import org.sopt.makers.crew.main.common.exception.BadRequestException;
import org.sopt.makers.crew.main.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MeetingRepository extends JpaRepository<Meeting, Integer>, MeetingSearchRepository {

	List<Meeting> findAllByUserId(Integer userId);

	List<Meeting> findAllByUser(User user);

	default Meeting findByIdOrThrow(Integer meetingId) {
		return findById(meetingId)
			.orElseThrow(() -> new BadRequestException(NOT_FOUND_MEETING.getErrorCode()));
	}

	@Query("SELECT m FROM Meeting m JOIN FETCH m.user ORDER BY m.id DESC LIMIT 20")
	List<Meeting> findTop20ByOrderByIdDesc();
}