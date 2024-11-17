package org.sopt.makers.crew.main.entity.apply;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.util.List;

import org.sopt.makers.crew.main.entity.apply.enums.EnApplyStatus;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.internal.dto.ApprovedStudyCountProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ApplyRepository extends JpaRepository<Apply, Integer>, ApplySearchRepository {

	@Query("select a from Apply a join fetch a.meeting m where a.userId = :userId and a.status = :statusValue")
	List<Apply> findAllByUserIdAndStatus(@Param("userId") Integer userId,
		@Param("statusValue") EnApplyStatus statusValue);

	@Query("select a "
		+ "from Apply a "
		+ "join fetch a.meeting m "
		+ "join fetch m.user u "
		+ "where a.userId = :userId "
		+ "ORDER BY a.id DESC ")
	List<Apply> findAllByUserIdOrderByIdDesc(@Param("userId") Integer userId);

	@Query("select a "
		+ "from Apply a "
		+ "join fetch a.user u "
		+ "where a.meetingId = :meetingId "
		+ "and a.status in :statuses order by :order")
	List<Apply> findAllByMeetingIdWithUser(@Param("meetingId") Integer meetingId,
		@Param("statuses") List<EnApplyStatus> statuses, @Param("order") String order);

	List<Apply> findAllByMeetingIdAndStatus(Integer meetingId, EnApplyStatus statusValue);

	List<Apply> findAllByMeetingId(Integer meetingId);

	List<Apply> findAllByMeetingIdIn(List<Integer> meetingIds);

	boolean existsByMeetingIdAndUserId(Integer meetingId, Integer userId);

	@Transactional
	@Modifying(clearAutomatically = true)
	@Query("delete from Apply a where a.meeting.id = :meetingId and a.userId = :userId")
	void deleteByMeetingIdAndUserId(@Param("meetingId") Integer meetingId, @Param("userId") Integer userId);

	default Apply findByIdOrThrow(Integer applyId) {
		return findById(applyId)
			.orElseThrow(() -> new BadRequestException(NOT_FOUND_APPLY.getErrorCode()));
	}

	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("DELETE FROM Apply a WHERE a.meetingId = :meetingId")
	void deleteAllByMeetingIdQuery(Integer meetingId);

	@Query("SELECT u.orgId AS orgId, COUNT(a.id) AS approvedStudyCount " +
		"FROM Apply a JOIN a.meeting m " +
		"JOIN a.user u " +
		"WHERE m.category = :category AND a.status = :status AND u.orgId = :orgId " +
		"GROUP BY u.orgId")
	List<ApprovedStudyCountProjection> findApprovedStudyCountByOrgId(
		@Param("category") MeetingCategory category,
		@Param("status") EnApplyStatus status,
		@Param("orgId") Integer orgId);
}
