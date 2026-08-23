package org.sopt.makers.crew.main.slack;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.slack.api.bolt.App;
import com.slack.api.model.event.ReactionAddedEvent;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * dev 환경 lambda 이관으로 인한 prod 환경에서 작동될 수 있도록 변동!
 *
 * @author khyojun
 */
@Slf4j
@Component
@Profile({"prod"})
@RequiredArgsConstructor
public class SlackEventListener {

	private final App slackApp;
	private final SlackMessageService slackMessageService;

	@PostConstruct
	public void init() {
		log.info("Starting Slack event listener");
		slackApp.event(ReactionAddedEvent.class, ((payload, ctx) -> {
			try {
				ReactionAddedEvent event = payload.getEvent();
				String emoji = event.getReaction();

				log.info("Reaction added - Emoji: {}, User: {}, Channel: {}",
					emoji, event.getUser(), event.getItem().getChannel());

				slackMessageService.sendMention(ctx.client(), event);
			} catch (Exception e) {
				log.warn("Failed to process reaction event", e);
			}

			return ctx.ack();
		}));
	}

}
