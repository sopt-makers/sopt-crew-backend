package org.sopt.makers.crew.main.slack;

import org.sopt.makers.crew.main.entity.slack.MakersUserSlack;
import org.sopt.makers.crew.main.entity.slack.MakersUserSlackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.model.event.ReactionAddedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SlackMesasgeService {

	private final MakersUserSlackRepository makersUserSlackRepository;

	@Transactional
	public void insertUser() {

	}

	public void sendMention(MethodsClient client, ReactionAddedEvent event) {
		try {

			String channel = event.getItem().getChannel();
			String user = event.getUser();
			String reaction = event.getReaction();

			MakersUserSlack slackUser = makersUserSlackRepository.findByUserName(reaction)
				.orElseThrow(() -> new IllegalArgumentException("slack 전송 오류"));

			String message = SlackMessage.CALL_MESSAGE.getMessage()
				.replace("{callUser}", SlackUtils.mentionFormattingUser(user))
				.replace("{user}", SlackUtils.mentionFormattingUser(slackUser.getUserSlackId()));

			ChatPostMessageResponse responses = client.chatPostMessage(r -> r
				.channel(channel)
				.text(message)
				.threadTs(event.getItem().getTs()));

			if (responses.isOk())
				log.info("message send success");
			else {
				throw new RuntimeException("ERROR OCCURED");
			}
		} catch (Exception e) {
			log.error("now error : {}", e);
			log.error("message send failed");
		}
	}

}
