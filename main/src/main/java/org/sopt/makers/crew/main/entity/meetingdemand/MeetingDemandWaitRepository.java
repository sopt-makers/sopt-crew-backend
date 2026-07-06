package org.sopt.makers.crew.main.entity.meetingdemand;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MeetingDemandWaitRepository extends JpaRepository<MeetingDemandWait, Integer> {

	boolean existsByMeetingDemandIdAndUserId(Integer meetingDemandId, Integer userId);

	int deleteByMeetingDemandIdAndUserId(Integer meetingDemandId, Integer userId);

	void deleteAllByMeetingDemandId(Integer meetingDemandId);

	long countByMeetingDemandId(Integer meetingDemandId);

	@Query("SELECT w.userId FROM MeetingDemandWait w WHERE w.meetingDemandId = :meetingDemandId")
	List<Integer> findUserIdsByMeetingDemandId(@Param("meetingDemandId") Integer meetingDemandId);

	List<MeetingDemandWait> findAllByMeetingDemandIdInAndUserId(List<Integer> meetingDemandIds, Integer userId);
}
