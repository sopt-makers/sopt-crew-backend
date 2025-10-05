package org.sopt.makers.crew.main.slack;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SlackMessage {

	CALL_MESSAGE("{callUser}님이 {user}를 찾고 있으신가봐요! 빨리 나와주세요!"),
	;

	private final String message;
}
