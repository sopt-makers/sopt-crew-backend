package org.sopt.makers.crew.main.global.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MemberMentionConvertUtils {

	public static String convertMentionFormatToPg(String contents) {
		if (contents == null) {
			return null;
		}
		return contents.replaceAll("-~!@#@([^\\[]+)\\[(\\d+)\\]%\\^&\\*\\+", "@$1[$2]");
	}

	public static String convertMentionFormatToCrew(String contents) {
		if (contents == null) {
			return null;
		}
		return contents.replaceAll("@([^\\[]+)\\[(\\d+)\\]", "-~!@#@$1[$2]%^&*+");
	}

}
