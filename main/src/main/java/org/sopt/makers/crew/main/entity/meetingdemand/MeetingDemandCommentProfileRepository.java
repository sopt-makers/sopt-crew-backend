package org.sopt.makers.crew.main.entity.meetingdemand;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingDemandCommentProfileRepository extends JpaRepository<MeetingDemandCommentProfile, Integer> {

	Optional<MeetingDemandCommentProfile> findByMeetingDemandIdAndUserId(Integer meetingDemandId, Integer userId);

	List<MeetingDemandCommentProfile> findAllByMeetingDemandIdAndUserIdIn(Integer meetingDemandId, Set<Integer> userIds);
}
