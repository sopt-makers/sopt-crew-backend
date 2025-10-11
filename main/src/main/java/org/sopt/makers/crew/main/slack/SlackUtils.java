package org.sopt.makers.crew.main.slack;

import java.util.List;
import java.util.stream.Collectors;

public class SlackUtils {

	public static String mentionFormattingUser(String slackUserId) {
		return String.format("<@%s>", slackUserId);
	}

	public static String mentionFormattingUsers(List<String> slackUserIds, String delimiter) {
		return slackUserIds.stream()
			.map(SlackUtils::mentionFormattingUser)
			.collect(Collectors.joining(delimiter));
	}
}
