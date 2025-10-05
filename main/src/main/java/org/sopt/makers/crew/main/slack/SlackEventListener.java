package org.sopt.makers.crew.main.slack;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.slack.api.bolt.App;
import com.slack.api.model.event.ReactionAddedEvent;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Profile({"dev"})
@RequiredArgsConstructor
public class SlackEventListener {

	private final App slackApp;
	private final SlackMesasgeService slackMessageService;

	@PostConstruct
	public void init() {
		slackApp.event(ReactionAddedEvent.class, ((payload, ctx) -> {
			ReactionAddedEvent event = payload.getEvent();
			String emoji = event.getReaction();

			log.info("Reaction added - Emoji: {}, User: {}, Channel: {}",
				emoji, event.getUser(), event.getItem().getChannel());

			slackMessageService.sendMention(ctx.client(), event);

			return ctx.ack();
		}));
	}

}
