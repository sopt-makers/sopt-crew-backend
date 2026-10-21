package org.sopt.makers.crew.main.entity.dining;

import java.util.List;
import java.util.Optional;

import org.sopt.makers.crew.main.entity.dining.enums.DiningPayerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DiningAttendanceRepository extends JpaRepository<DiningAttendance, Integer> {

	boolean existsByRoundIdAndUserId(Integer roundId, Integer userId);

	Optional<DiningAttendance> findByRoundIdAndUserId(Integer roundId, Integer userId);

	List<DiningAttendance> findAllByRoundIdIn(List<Integer> roundIds);

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
}
