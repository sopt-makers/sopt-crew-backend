package org.sopt.makers.crew.main.entity.post;

import static org.sopt.makers.crew.main.common.exception.ErrorStatus.NOT_FOUND_POST;

import java.util.List;
import java.util.Optional;

import org.sopt.makers.crew.main.common.exception.BadRequestException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface PostRepository extends JpaRepository<Post, Integer>, PostSearchRepository {

	default Post findByIdOrThrow(Integer postId) {
		return findById(postId)
			.orElseThrow(() -> new BadRequestException(NOT_FOUND_POST.getErrorCode()));
	}

	Integer countByMeetingId(Integer meetingId);

	List<Post> findAllByMeetingId(Integer meetingId);

	List<Post> findAllByMeetingIdIn(List<Integer> meetingIds);

	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("DELETE FROM Post p WHERE p.meetingId = :meetingId")
	void deleteAllByMeetingIdQuery(Integer meetingId);
}
