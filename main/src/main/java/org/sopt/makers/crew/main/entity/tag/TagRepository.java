package org.sopt.makers.crew.main.entity.tag;

import java.util.List;
import java.util.Optional;

import org.sopt.makers.crew.main.entity.tag.projection.MeetingKeywordsTypeProjection;
import org.sopt.makers.crew.main.entity.tag.projection.MeetingTagInfoProjection;
import org.sopt.makers.crew.main.entity.tag.projection.WelcomeMessageTypeProjection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Integer> {
	Optional<WelcomeMessageTypeProjection> findWelcomeMessageTypesByFlashId(Integer flashId);

	Optional<WelcomeMessageTypeProjection> findWelcomeMessageTypesByMeetingId(Integer meetingId);

	Optional<MeetingKeywordsTypeProjection> findMeetingKeywordTypesByFlashId(Integer flashId);

	Optional<MeetingKeywordsTypeProjection> findMeetingKeywordTypesByMeetingId(Integer meetingId);

	Optional<Tag> findTagByFlashId(Integer flashId);

	Optional<Tag> findTagByMeetingId(Integer meetingId);

	void deleteByFlashId(Integer flashId);

	void deleteByMeetingId(Integer meetingId);

	List<MeetingTagInfoProjection> findByMeetingIdIn(List<Integer> meetingIds);
}
