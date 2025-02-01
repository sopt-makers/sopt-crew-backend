package org.sopt.makers.crew.main.entity.tag;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Integer> {
	Optional<WelcomeMessageTypeProjection> findByFlashId(Integer flashId);

	Optional<Tag> findTagByFlashId(Integer flashId);

	void deleteByFlashId(Integer flashId);
}
