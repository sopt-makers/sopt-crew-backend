package org.sopt.makers.crew.main.slack;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class MessageContext {
	private String callUser;
	private String calledUser;

	public static MessageContext create(String user, String userSlackId) {
		return MessageContext
			.builder()
			.callUser(user)
			.calledUser(userSlackId)
			.build();
	}
}
