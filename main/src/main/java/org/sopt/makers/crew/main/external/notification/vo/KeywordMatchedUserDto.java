package org.sopt.makers.crew.main.external.notification.vo;

import org.sopt.makers.crew.main.entity.user.User;

public record KeywordMatchedUserDto(User user) {

	public static KeywordMatchedUserDto of(User user) {
		return new KeywordMatchedUserDto(user);
	}
}
