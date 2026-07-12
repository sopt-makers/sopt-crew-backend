package org.sopt.makers.crew.main.entity.meetingdemandcomment;

import java.util.List;
import java.util.Optional;

import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.global.exception.ErrorStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface MeetingDemandCommentRepository extends JpaRepository<MeetingDemandComment, Integer> {

	default MeetingDemandComment findByIdOrThrow(Integer commentId) {
		return findById(commentId)
			.orElseThrow(() -> new BadRequestException(ErrorStatus.NOT_FOUND_COMMENT.getErrorCode()));
	}

	Optional<MeetingDemandComment> findFirstByParentIdOrderByOrderDesc(Integer parentId);

	List<MeetingDemandComment> findAllByParentIdAndDepthOrderByOrderDesc(Integer parentId, int depth);

	List<MeetingDemandComment> findAllByParentIdInAndDepthOrderByParentIdAscOrderAsc(List<Integer> parentIds,
		int depth);

	Optional<MeetingDemandComment> findByIdAndMeetingDemandId(Integer id, Integer meetingDemandId);

	default MeetingDemandComment findByIdAndMeetingDemandIdOrThrow(Integer id, Integer meetingDemandId) {
		return findByIdAndMeetingDemandId(id, meetingDemandId)
			.orElseThrow(() -> new BadRequestException(ErrorStatus.NOT_FOUND_COMMENT.getErrorCode()));
	}

	List<MeetingDemandComment> findAllByMeetingDemandIdOrderByCreatedTimestampAsc(Integer meetingDemandId);

	@Query("SELECT DISTINCT c.userId "
		+ "FROM MeetingDemandComment c "
		+ "WHERE c.meetingDemandId = :meetingDemandId "
		+ "AND c.userId IS NOT NULL")
	List<Integer> findDistinctUserIdsByMeetingDemandId(@Param("meetingDemandId") Integer meetingDemandId);

	Page<MeetingDemandComment> findAllByMeetingDemandIdAndDepth(Integer meetingDemandId, int depth, Pageable pageable);

	int countByMeetingDemandIdAndDepth(Integer meetingDemandId, int depth);

	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("DELETE FROM MeetingDemandComment c WHERE c.meetingDemandId = :meetingDemandId")
	void deleteAllByMeetingDemandId(Integer meetingDemandId);
}
