package org.sopt.makers.crew.main.entity.meetingdemand;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingDemandWaitRepository extends JpaRepository<MeetingDemandWait, Integer> {

	boolean existsByMeetingDemandIdAndUserId(Integer meetingDemandId, Integer userId);

	List<MeetingDemandWait> findAllByMeetingDemandIdInAndUserId(List<Integer> meetingDemandIds, Integer userId);
}
