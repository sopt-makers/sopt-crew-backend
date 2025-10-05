package org.sopt.makers.crew.main.slack;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;

@Configuration
public class SlackConfig {

	@Value("${slack.bot-token}")
	private String botToken;

	@Bean
	public AppConfig appConfig() {
		return AppConfig.builder()
			.singleTeamBotToken(botToken)
			.build();
	}

	@Bean
	public App slackApp(AppConfig appConfig) {
		return new App(appConfig);
	}

}
