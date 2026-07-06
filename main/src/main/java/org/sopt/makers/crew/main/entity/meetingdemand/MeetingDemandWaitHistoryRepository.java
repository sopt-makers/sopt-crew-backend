package org.sopt.makers.crew.main.entity.meetingdemand;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingDemandWaitHistoryRepository extends JpaRepository<MeetingDemandWaitHistory, Integer> {

	boolean existsByMeetingDemandIdAndUserId(Integer meetingDemandId, Integer userId);
}
