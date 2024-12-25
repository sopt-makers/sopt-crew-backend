package org.sopt.makers.crew.main.entity.meeting;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CoLeaderRepository extends JpaRepository<CoLeader, Integer> {

	void deleteAllByMeetingId(Integer meetingId);

	@Query("SELECT c FROM CoLeader c JOIN FETCH c.user WHERE c.meeting.id =:meetingId")
	List<CoLeader> findAllByMeetingId(Integer meetingId);

	List<CoLeader> findAllByMeetingIdIn(List<Integer> meetingId);

	List<CoLeader> findAllByUserId(Integer userId);

	@Query("SELECT c FROM CoLeader c JOIN FETCH c.meeting WHERE c.user.id =:userId")
	List<CoLeader> findAllByUserIdWithMeeting(Integer userId);

}