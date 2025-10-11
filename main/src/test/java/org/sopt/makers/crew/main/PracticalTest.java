package org.sopt.makers.crew.main;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.sopt.makers.crew.main.slack.SlackUtils;

public class PracticalTest {

	@Test
	public void test() {
		List<String> strings = List.of("a", "b", "c");

		String s = SlackUtils.mentionFormattingUsers(strings, " ");

		System.out.println(s);
	}
}
