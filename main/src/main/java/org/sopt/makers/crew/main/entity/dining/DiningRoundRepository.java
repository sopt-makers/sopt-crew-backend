package org.sopt.makers.crew.main.entity.dining;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.time.LocalDateTime;
import java.util.List;

import org.sopt.makers.crew.main.entity.dining.enums.DiningRoundStatus;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;
import org.sopt.makers.crew.main.global.exception.NotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DiningRoundRepository extends JpaRepository<DiningRound, Integer> {

	default DiningRound findByIdOrThrow(Integer roundId) {
		return findById(roundId)
			.orElseThrow(() -> new NotFoundException(NOT_FOUND_DINING_ROUND.getErrorCode()));
	}

	/**
	 * @implSpec : 회식의 회차를 1차부터 순서대로 조회한다. (인덱스 + 1 = 회차 번호)
	 */
	List<DiningRound> findAllByMeetingIdOrderByIdAsc(Integer meetingId);

	List<DiningRound> findAllByMeetingIdInOrderByIdAsc(List<Integer> meetingIds);

	/**
	 * @implSpec : 자동 확정. 회식 일자가 기준일(today) 이전인 회식의 미마감 회차를 한 번에 마감한다.
	 */
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("UPDATE DiningRound r SET r.status = :closed, r.closedAt = :now, r.modifiedTimestamp = :now "
		+ "WHERE r.status = :open AND r.meetingId IN "
		+ "(SELECT m.id FROM Meeting m WHERE m.category = :category AND m.mStartDate < :today)")
	int closeOpenRoundsOfDiningsBefore(@Param("closed") DiningRoundStatus closed,
		@Param("open") DiningRoundStatus open, @Param("category") MeetingCategory category,
		@Param("today") LocalDateTime today, @Param("now") LocalDateTime now);
}
