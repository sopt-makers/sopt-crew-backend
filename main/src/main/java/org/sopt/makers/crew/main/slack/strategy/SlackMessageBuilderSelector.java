package org.sopt.makers.crew.main.slack.strategy;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SlackMessageBuilderSelector {

	private final List<SlackMessageBuilder> slackMessageBuilders;

	public SlackMessageBuilder selectSlackMessageBuilder(String templateCd) {
		return slackMessageBuilders.stream()
			.filter(s -> s.isSelectedBuilder(templateCd))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("No builder found for template: " + templateCd));
	}
}
