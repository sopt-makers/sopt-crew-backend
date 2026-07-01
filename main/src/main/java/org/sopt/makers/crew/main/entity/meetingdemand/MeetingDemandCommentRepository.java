package org.sopt.makers.crew.main.entity.meetingdemand;

import java.util.List;
import java.util.Optional;

import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.global.exception.ErrorStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface MeetingDemandCommentRepository extends JpaRepository<MeetingDemandComment, Integer> {

	default MeetingDemandComment findByIdOrThrow(Integer commentId) {
		return findById(commentId)
			.orElseThrow(() -> new BadRequestException(ErrorStatus.NOT_FOUND_COMMENT.getErrorCode()));
	}

	Optional<MeetingDemandComment> findFirstByParentIdOrderByOrderDesc(Integer parentId);

	List<MeetingDemandComment> findAllByParentIdAndDepthOrderByOrderDesc(Integer parentId, int depth);

	Optional<MeetingDemandComment> findByIdAndMeetingDemandId(Integer id, Integer meetingDemandId);

	default MeetingDemandComment findByIdAndMeetingDemandIdOrThrow(Integer id, Integer meetingDemandId) {
		return findByIdAndMeetingDemandId(id, meetingDemandId)
			.orElseThrow(() -> new BadRequestException(ErrorStatus.NOT_FOUND_COMMENT.getErrorCode()));
	}

	List<MeetingDemandComment> findAllByMeetingDemandIdOrderByCreatedTimestampAsc(Integer meetingDemandId);

	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("DELETE FROM MeetingDemandComment c WHERE c.meetingDemandId = :meetingDemandId")
	void deleteAllByMeetingDemandId(Integer meetingDemandId);
}
