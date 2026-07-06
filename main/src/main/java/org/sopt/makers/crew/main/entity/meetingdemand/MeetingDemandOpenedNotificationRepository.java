package org.sopt.makers.crew.main.entity.meetingdemand;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MeetingDemandOpenedNotificationRepository
	extends JpaRepository<MeetingDemandOpenedNotification, Integer> {

	Optional<MeetingDemandOpenedNotification> findByMeetingId(Integer meetingId);

	@Query("SELECT n "
		+ "FROM MeetingDemandOpenedNotification n "
		+ "JOIN Meeting m ON m.id = n.meetingId "
		+ "WHERE n.sentAt IS NULL "
		+ "AND m.meetingDemandId IS NOT NULL "
		+ "AND m.startDate <= :now "
		+ "AND m.endDate > :now")
	List<MeetingDemandOpenedNotification> findAllUnsentApplyAble(@Param("now") LocalDateTime now);
}
