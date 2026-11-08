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
	 * @implSpec : 회차·회식 삭제 시 스냅샷 생성 전에 회차를 id 순서로 먼저 잠근다.
	 * 참석 추가 INSERT의 FK 검사(KEY SHARE)와 충돌해 삭제 중 들어온 참석이 이력 없이 CASCADE 삭제되지 않는다.
	 * Hibernate 6의 PESSIMISTIC_WRITE는 PostgreSQL에서 FOR NO KEY UPDATE로 나가 KEY SHARE와 충돌하지 않으므로
	 * native query로 FOR UPDATE를 명시한다.
	 */
	@Query(value = "SELECT * FROM dining_round WHERE \"meetingId\" = :meetingId ORDER BY id FOR UPDATE",
		nativeQuery = true)
	List<DiningRound> findAllByMeetingIdForUpdate(@Param("meetingId") Integer meetingId);

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
