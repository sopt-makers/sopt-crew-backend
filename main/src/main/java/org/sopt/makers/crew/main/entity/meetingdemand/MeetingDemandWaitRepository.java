package org.sopt.makers.crew.main.entity.meetingdemand;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingDemandWaitRepository extends JpaRepository<MeetingDemandWait, Integer> {

	boolean existsByMeetingDemandIdAndUserId(Integer meetingDemandId, Integer userId);

	int deleteByMeetingDemandIdAndUserId(Integer meetingDemandId, Integer userId);

	void deleteAllByMeetingDemandId(Integer meetingDemandId);

	long countByMeetingDemandId(Integer meetingDemandId);

	List<MeetingDemandWait> findAllByMeetingDemandIdInAndUserId(List<Integer> meetingDemandIds, Integer userId);
}
