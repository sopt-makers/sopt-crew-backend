package org.sopt.makers.crew.main.slack;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.slack.api.bolt.App;
import com.slack.api.bolt.socket_mode.SocketModeApp;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Profile({"dev"})
@Component
@Slf4j
@RequiredArgsConstructor
public class SlackSocketModeStarter {

	private final App slackApp;

	@Value("${slack.app-token}")
	private String appToken;

	private SocketModeApp socketModeApp;

	@PostConstruct
	public void init() {
		try {
			socketModeApp = new SocketModeApp(appToken, slackApp);
			socketModeApp.startAsync();
			log.info("slack socket mode start");
			log.info("now slack app token was this : {}", appToken);
		} catch (Exception e) {
			log.error("slack socket mode error");
		}
	}

	@PreDestroy
	public void destroy() {
		try {
			socketModeApp.stop();
			log.info("slack socket mode stop");
		} catch (Exception e) {
			log.error("slack socket mode error");
		}
	}
}
