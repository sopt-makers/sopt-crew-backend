package org.sopt.makers.crew.main.entity.dining;

import java.util.List;
import java.util.Optional;

import org.sopt.makers.crew.main.entity.dining.enums.DiningPayerStatus;
import org.sopt.makers.crew.main.entity.dining.projection.DiningAttendeeCountProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DiningAttendanceRepository extends JpaRepository<DiningAttendance, Integer> {

	boolean existsByRoundIdAndUserId(Integer roundId, Integer userId);

	Optional<DiningAttendance> findByRoundIdAndUserId(Integer roundId, Integer userId);

	List<DiningAttendance> findAllByRoundIdIn(List<Integer> roundIds);

	List<DiningAttendance> findAllByRoundId(Integer roundId);

	@Query("SELECT DISTINCT a.userId FROM DiningAttendance a WHERE a.roundId IN :roundIds")
	List<Integer> findDistinctUserIdsByRoundIdIn(@Param("roundIds") List<Integer> roundIds);

	long countByRoundId(Integer roundId);

	/**
	 * @implSpec : 회식 단위 권한 확인. 해당 회식의 회차 중 하나라도 주어진 결제자 상태면 true
	 */
	@Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END "
		+ "FROM DiningAttendance a, DiningRound r "
		+ "WHERE a.roundId = r.id "
		+ "AND r.meetingId = :meetingId "
		+ "AND a.userId = :userId "
		+ "AND a.payerStatus = :payerStatus")
	boolean existsByMeetingIdAndUserIdAndPayerStatus(@Param("meetingId") Integer meetingId,
		@Param("userId") Integer userId, @Param("payerStatus") DiningPayerStatus payerStatus);

	@Query("SELECT COUNT(DISTINCT a.userId) FROM DiningAttendance a, DiningRound r "
		+ "WHERE a.roundId = r.id AND r.meetingId = :meetingId")
	long countDistinctUserIdsByMeetingId(@Param("meetingId") Integer meetingId);

	/**
	 * 목록 카드용 고유 참석자 수 일괄 조회. 결과에 없는 회식의 참석자 수는 0으로 처리한다.
	 */
	@Query("SELECT r.meetingId AS meetingId, COUNT(DISTINCT a.userId) AS attendeeCount "
		+ "FROM DiningAttendance a, DiningRound r "
		+ "WHERE a.roundId = r.id AND r.meetingId IN :meetingIds GROUP BY r.meetingId")
	List<DiningAttendeeCountProjection> countDistinctUserIdsByMeetingIdIn(
		@Param("meetingIds") List<Integer> meetingIds);
}
