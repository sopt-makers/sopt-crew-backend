package org.sopt.makers.crew.main.entity.dining;

import java.util.List;
import java.util.Optional;

import org.sopt.makers.crew.main.entity.dining.enums.DiningPayerStatus;
import org.sopt.makers.crew.main.entity.dining.projection.DiningAttendeeCountProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface DiningAttendanceRepository extends JpaRepository<DiningAttendance, Integer> {

	boolean existsByRoundIdAndUserId(Integer roundId, Integer userId);

	Optional<DiningAttendance> findByRoundIdAndUserId(Integer roundId, Integer userId);

	/** 결제자 상태 변경과 참석 삭제를 직렬화해 삭제 이력에 마지막 커밋 상태를 보관한다. */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT a FROM DiningAttendance a WHERE a.roundId = :roundId AND a.userId = :userId")
	Optional<DiningAttendance> findByRoundIdAndUserIdForUpdate(@Param("roundId") Integer roundId,
		@Param("userId") Integer userId);

	/** 회차·회식 삭제 시 스냅샷 생성 전에 잠금을 확보한다. 일괄 삭제끼리는 같은 ID 순서로 잠근다. */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT a FROM DiningAttendance a WHERE a.roundId IN :roundIds ORDER BY a.id ASC")
	List<DiningAttendance> findAllByRoundIdInForUpdate(@Param("roundIds") List<Integer> roundIds);

	List<DiningAttendance> findAllByRoundIdInAndPayerStatusOrderByPayerRequestedAtAscIdAsc(
		List<Integer> roundIds, DiningPayerStatus payerStatus);

	List<DiningAttendance> findAllByRoundIdIn(List<Integer> roundIds);

	List<DiningAttendance> findAllByRoundId(Integer roundId);

	List<DiningAttendance> findAllByRoundIdOrderByCreatedTimestampAscIdAsc(Integer roundId);

	Page<DiningAttendance> findAllByRoundId(Integer roundId, Pageable pageable);

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

	/**
	 * 목록 카드용. 주어진 회식 중 사용자가 해당 결제자 상태인 회차가 있는 회식 id
	 */
	@Query("SELECT DISTINCT r.meetingId FROM DiningAttendance a, DiningRound r "
		+ "WHERE a.roundId = r.id AND a.userId = :userId AND a.payerStatus = :payerStatus "
		+ "AND r.meetingId IN :meetingIds")
	List<Integer> findMeetingIdsByUserIdAndPayerStatus(@Param("userId") Integer userId,
		@Param("payerStatus") DiningPayerStatus payerStatus, @Param("meetingIds") List<Integer> meetingIds);

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
