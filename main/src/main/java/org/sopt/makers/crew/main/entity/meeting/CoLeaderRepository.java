package org.sopt.makers.crew.main.entity.meeting;

import java.util.List;

import org.sopt.makers.crew.main.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoLeaderRepository extends JpaRepository<CoLeader, Integer> {

	void deleteAllByMeetingId(Integer meetingId);

	List<CoLeader> findAllByMeetingId(Integer meetingId);

	List<CoLeader> findAllByMeetingIdIn(List<Integer> meetingId);

	List<CoLeader> findAllByUserId(Integer userId);

}