package org.sopt.makers.crew.main.entity.slack;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SlackMessageTemplateRepository extends JpaRepository<SlackMessageTemplate, String> {
	Optional<SlackMessageTemplate> findByTemplateCd(String templateCd);
}
