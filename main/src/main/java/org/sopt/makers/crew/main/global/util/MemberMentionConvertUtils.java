package org.sopt.makers.crew.main.global.util;

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
