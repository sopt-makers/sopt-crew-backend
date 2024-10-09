package org.sopt.makers.crew.main.entity.meeting;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JointLeaderRepository extends JpaRepository<JointLeader, Integer> {

	void deleteAllByMeetingId(Integer meetingId);
}