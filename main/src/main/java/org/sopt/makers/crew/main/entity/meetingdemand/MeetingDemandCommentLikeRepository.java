package org.sopt.makers.crew.main.entity.meetingdemand;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingDemandCommentLikeRepository extends JpaRepository<MeetingDemandCommentLike, Integer> {

	boolean existsByMeetingDemandCommentIdAndUserId(Integer meetingDemandCommentId, Integer userId);

	int deleteByMeetingDemandCommentIdAndUserId(Integer meetingDemandCommentId, Integer userId);

	void deleteAllByMeetingDemandCommentId(Integer meetingDemandCommentId);

	List<MeetingDemandCommentLike> findAllByMeetingDemandCommentIdInAndUserId(List<Integer> commentIds,
		Integer userId);
}
