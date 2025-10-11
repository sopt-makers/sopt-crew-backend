package org.sopt.makers.crew.main.entity.slack;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MakersUserSlackRepository extends JpaRepository<MakersUserSlack, Long> {

	Optional<MakersUserSlack> findByCallEmojiAndUserSlackId(String callEmoji, String userSlackId);

	@Query("select m from makers_user_slack m where m.callEmoji = :emoji")
	List<MakersUserSlack> findByCallEmoji(@Param("emoji") String emoji);

	@Modifying
	void deleteByCallEmoji(String callEmoji);

	boolean existsByCallEmojiAndUserSlackId(String callEmoji, String userSlackId);

	@Modifying
	void deleteByCallEmojiAndUserSlackId(String callEmoji, String userSlackId);
}