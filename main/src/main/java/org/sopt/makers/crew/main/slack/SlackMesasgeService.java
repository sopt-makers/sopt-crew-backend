package org.sopt.makers.crew.main.slack;

import org.sopt.makers.crew.main.entity.slack.MakersUserSlack;
import org.sopt.makers.crew.main.entity.slack.MakersUserSlackRepository;
import org.sopt.makers.crew.main.entity.slack.SlackMessageTemplate;
import org.sopt.makers.crew.main.entity.slack.SlackMessageTemplateRepository;
import org.sopt.makers.crew.main.slack.dto.SlackEmojiEventDto;
import org.sopt.makers.crew.main.slack.dto.SlackEmojiEventDto.SlackDeleteEmojiEventDto;
import org.sopt.makers.crew.main.slack.dto.SlackEmojiEventDto.SlackUpdateEmojiEventDto;
import org.sopt.makers.crew.main.slack.strategy.SlackMessageBuilder;
import org.sopt.makers.crew.main.slack.strategy.SlackMessageBuilderSelector;
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
	private final SlackMessageTemplateRepository slackMessageTemplateRepository;
	private final SlackMessageBuilderSelector selector;

	@Transactional
	public void insertEvent(SlackEmojiEventDto dto) {
		if (makersUserSlackRepository.existsByCallEmoji((dto.getCallEmoji())))
			throw new IllegalArgumentException("Call Emoji already exists");

		makersUserSlackRepository.save(dto.toEntity());
	}

	@Transactional
	public void updateEvent(SlackUpdateEmojiEventDto dto) {
		MakersUserSlack makersUserSlack = makersUserSlackRepository.findByCallEmoji(dto.getCallEmoji())
			.orElseThrow(() -> new IllegalArgumentException("Invalid call emoji: " + dto.getCallEmoji()));

		makersUserSlack.updateEmoji(dto.getUpdateCallEmoji());
	}

	@Transactional
	public void deleteEvent(SlackDeleteEmojiEventDto dto) {
		if (!makersUserSlackRepository.existsByCallEmoji((dto.getCallEmoji())))
			throw new IllegalArgumentException("Call Emoji not exists");
		makersUserSlackRepository.deleteByCallEmoji(dto.getCallEmoji());
	}

	public void sendMention(MethodsClient client, ReactionAddedEvent event) {
		try {
			String channel = event.getItem().getChannel();
			String user = event.getUser();
			String reaction = event.getReaction();

			MakersUserSlack slackUser = makersUserSlackRepository.findByCallEmoji(reaction)
				.orElseThrow(() -> new IllegalArgumentException("slack 전송 오류"));

			String slackTemplateCd = slackUser.getSlackTemplateCd();
			SlackMessageTemplate slackMessageTemplate = slackMessageTemplateRepository.findByTemplateCd(slackTemplateCd)
				.orElseThrow(() -> new IllegalArgumentException("해당 슬랙 메시지 템플릿이 존재하지 않습니다."));

			SlackMessageBuilder slackMessageBuilder = selector.selectSlackMessageBuilder(slackTemplateCd);

			String sendMessage = slackMessageBuilder.buildSlackMessage(slackMessageTemplate.getTemplateContent(),
				MessageContext.create(user, slackUser.getUserSlackId()));

			ChatPostMessageResponse responses = client.chatPostMessage(r -> r
				.channel(channel)
				.text(sendMessage)
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
