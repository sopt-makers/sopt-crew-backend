package org.sopt.makers.crew.main.slack;

import java.io.IOException;
import java.util.List;

import org.sopt.makers.crew.main.entity.slack.MakersUserSlack;
import org.sopt.makers.crew.main.entity.slack.MakersUserSlackRepository;
import org.sopt.makers.crew.main.entity.slack.SlackMessageTemplate;
import org.sopt.makers.crew.main.entity.slack.SlackMessageTemplateRepository;
import org.sopt.makers.crew.main.global.exception.CustomSlackException;
import org.sopt.makers.crew.main.slack.dto.SlackEmojiEventDto;
import org.sopt.makers.crew.main.slack.dto.SlackEmojiEventDto.SlackDeleteEmojiEventDto;
import org.sopt.makers.crew.main.slack.dto.SlackEmojiEventDto.SlackUpdateEmojiEventDto;
import org.sopt.makers.crew.main.slack.strategy.SlackMessageBuilder;
import org.sopt.makers.crew.main.slack.strategy.SlackMessageBuilderSelector;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.model.event.ReactionAddedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SlackMessageService {

	private final MakersUserSlackRepository makersUserSlackRepository;
	private final SlackMessageTemplateRepository slackMessageTemplateRepository;
	private final SlackMessageBuilderSelector selector;

	@Transactional
	public void insertEvent(SlackEmojiEventDto dto) {
		if (makersUserSlackRepository.existsByCallEmojiAndUserSlackId(dto.getCallEmoji(), dto.getUserSlackId()))
			throw new CustomSlackException("Call Emoji already exists");

		makersUserSlackRepository.save(dto.toEntity());
	}

	@Transactional
	public void updateEvent(SlackUpdateEmojiEventDto dto) {
		MakersUserSlack makersUserSlack = makersUserSlackRepository.findByCallEmojiAndUserSlackId(dto.getCallEmoji(),
				dto.getUserSlackId())
			.orElseThrow(() -> new CustomSlackException(
				"Invalid call emoji or user slack Id : " + dto.getCallEmoji() + " " + dto.getUserSlackId()));

		makersUserSlack.updateEmoji(dto.getUpdateCallEmoji());
	}

	@Transactional
	public void deleteEvent(SlackDeleteEmojiEventDto dto) {
		if (!makersUserSlackRepository.existsByCallEmojiAndUserSlackId(dto.getCallEmoji(), dto.getUserSlackId()))
			throw new CustomSlackException("Call Emoji not exists");

		makersUserSlackRepository.deleteByCallEmojiAndUserSlackId(dto.getCallEmoji(), dto.getUserSlackId());
	}

	public void sendMention(MethodsClient client, ReactionAddedEvent event) {
		String channel = event.getItem().getChannel();
		String user = event.getUser();
		try {

			List<MakersUserSlack> slackUser = resolveSlackUser(event);

			String sendMessage = messageBuild(slackUser, user);

			ChatPostMessageResponse responses = client.chatPostMessage(r -> r
				.channel(channel)
				.text(sendMessage)
				.threadTs(event.getItem().getTs()));

			if (responses.isOk())
				log.info("message send success");
			else {
				throw new CustomSlackException("ERROR OCCURED");
			}
		} catch (CustomSlackException | SlackApiException | IOException e) {
			log.warn("now error : {}", e);
			log.warn("message send failed");
		}
	}

	private List<MakersUserSlack> resolveSlackUser(ReactionAddedEvent event) {
		String reaction = event.getReaction();

		List<MakersUserSlack> slackUser = makersUserSlackRepository.findByCallEmoji(reaction);

		if (slackUser.isEmpty())
			throw new CustomSlackException("Invalid reaction emoji : " + reaction);

		return slackUser;
	}

	private String messageBuild(List<MakersUserSlack> slackUsers, String user) {

		String slackTemplateCd = extractTemplateCd(slackUsers);
		SlackMessageTemplate slackMessageTemplate = slackMessageTemplateRepository.findByTemplateCd(slackTemplateCd)
			.orElseThrow(() -> new CustomSlackException("해당 슬랙 메시지 템플릿이 존재하지 않습니다."));
		SlackMessageBuilder slackMessageBuilder = selector.selectSlackMessageBuilder(slackTemplateCd);
		String sendMessage = slackMessageBuilder.buildSlackMessage(slackMessageTemplate.getTemplateContent(),
			MessageContext.create(user, userIdList(slackUsers)));
		return sendMessage;
	}

	private List<String> userIdList(List<MakersUserSlack> slackUsers) {
		return slackUsers.stream().map(MakersUserSlack::getUserSlackId).toList();
	}

	private String extractTemplateCd(List<MakersUserSlack> slackUser) {
		if (slackUser.size() > 1) {
			return slackUser.stream()
				.map(MakersUserSlack::getSlackTemplateCd)
				.findFirst().orElseThrow(() -> new CustomSlackException("templateCd not found"));
		}

		return slackUser.get(0).getSlackTemplateCd();
	}

}
