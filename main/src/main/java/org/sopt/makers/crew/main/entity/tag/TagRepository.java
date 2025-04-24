package org.sopt.makers.crew.main.entity.tag;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Integer> {
	Optional<WelcomeMessageTypeProjection> findWelcomeMessageTypesByFlashId(Integer flashId);

	Optional<WelcomeMessageTypeProjection> findWelcomeMessageTypesByMeetingId(Integer meetingId);

	Optional<MeetingKeywordsTypeProjection> findMeetingKeywordTypesByFlashId(Integer flashId);

	Optional<MeetingKeywordsTypeProjection> findMeetingKeywordTypesByMeetingId(Integer meetingId);

	Optional<Tag> findTagByFlashId(Integer flashId);

	Optional<Tag> findTagByMeetingId(Integer meetingId);

	void deleteByFlashId(Integer flashId);
}
