package org.sopt.makers.crew.main.entity.tag;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TagRepository extends JpaRepository<Tag, Integer> {
	@Query("SELECT t.welcomeMessageTypes FROM Tag t WHERE t.lightningId = :lightningId")
	Optional<String> findWelcomeMessageTypesByLightningId(@Param("lightningId") Integer lightningId);

}
