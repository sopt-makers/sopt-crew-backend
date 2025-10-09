package org.sopt.makers.crew.main.slack.strategy;

import org.sopt.makers.crew.main.slack.MessageContext;

public interface SlackMessageBuilder {
	String buildSlackMessage(String text, MessageContext messageContext);

	boolean isSelectedBuilder(String templateCd);
}
