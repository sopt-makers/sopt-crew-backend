package org.sopt.makers.crew.main.slack;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class MessageContext {
	private String callUser;
	private List<String> calledUser;

	public static MessageContext create(String user, List<String> userSlackId) {
		return MessageContext
			.builder()
			.callUser(user)
			.calledUser(userSlackId)
			.build();
	}
}
