package org.sopt.makers.crew.main.entity.meeting;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JointLeaderRepository extends JpaRepository<JointLeader, Integer> {

	void deleteAllByMeetingId(Integer meetingId);

	List<JointLeader> findAllByMeetingId(Integer meetingId);

	List<JointLeader> findAllByMeetingIdIn(List<Integer> meetingId);

}