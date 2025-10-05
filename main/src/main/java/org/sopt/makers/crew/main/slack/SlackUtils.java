package org.sopt.makers.crew.main.slack;

public class SlackUtils {

	public static String mentionFormattingUser(String slackUserId) {
		return String.format("<@%s>", slackUserId);
	}
}
