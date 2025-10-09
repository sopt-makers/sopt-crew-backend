package org.sopt.makers.crew.main.slack.strategy;

import org.sopt.makers.crew.main.slack.MessageContext;
import org.sopt.makers.crew.main.slack.SlackUtils;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CallMessageBuilder implements SlackMessageBuilder {

	private static final String TEMPLATE_CODE = "call_message";

	@Override
	public String buildSlackMessage(String text, MessageContext messageContext) {
		return text
			.replace("{callUser}", SlackUtils.mentionFormattingUser(messageContext.getCallUser()))
			.replace("{user}", SlackUtils.mentionFormattingUser(messageContext.getCalledUser()));
	}

	@Override
	public boolean isSelectedBuilder(String templateCd) {
		return templateCd.equals(TEMPLATE_CODE);
	}
}
