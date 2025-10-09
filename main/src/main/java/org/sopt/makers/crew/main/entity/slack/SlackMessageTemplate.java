package org.sopt.makers.crew.main.entity.slack;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "slack_message_template")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SlackMessageTemplate {

	@Id
	@Column(name = "template_cd")
	private String templateCd;

	@Column(name = "template_content")
	private String templateContent;
}
