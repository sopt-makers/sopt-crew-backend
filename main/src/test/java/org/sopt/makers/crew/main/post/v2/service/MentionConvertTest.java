package org.sopt.makers.crew.main.post.v2.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sopt.makers.crew.main.global.util.MemberMentionConvertUtils;

public class MentionConvertTest {

	@Test
	void 크루_멘션을_플그_멘션으로_변환합니다() {
		String orginalCrewMention = "-~!@#@김효준[9999999]%^&*+";
		String changeMention = "@김효준[9999999]";

		Assertions.assertThat(MemberMentionConvertUtils.convertMentionFormat(orginalCrewMention))
			.isEqualTo(changeMention);

	}

	@Test
	void 텍스트_중간의_크루_멘션을_플그_멘션으로_변환합니다() {
		String orginalCrewMention = "안녕하세요-~!@#@김효준[9999999]%^&*+안녕하세요";
		String changeMention = "안녕하세요@김효준[9999999]안녕하세요";

		Assertions.assertThat(MemberMentionConvertUtils.convertMentionFormat(orginalCrewMention))
			.isEqualTo(changeMention);

	}

	@Test
	void 플그_멘션을_크루_멘션으로_변환합니다() {
		String orginalCrewMention = "안녕하세요-~!@#@김효준[9999999]%^&*+안녕하세요";
		String changeMention = "안녕하세요@김효준[9999999]안녕하세요";

		Assertions.assertThat(MemberMentionConvertUtils.convertMentionFormatToCrew(changeMention))
			.isEqualTo(orginalCrewMention);
	}

	@Test
	void 다중_크루_멘션을_플그_멘션으로_변환합니다() {
		String orginalCrewMention = "안녕하세요-~!@#@김효준[9999999]%^&*+-~!@#@이동훈[1234123412]%^&*+안녕하세요";
		String changeMention = "안녕하세요@김효준[9999999]@이동훈[1234123412]안녕하세요";

		Assertions.assertThat(MemberMentionConvertUtils.convertMentionFormat(orginalCrewMention))
			.isEqualTo(changeMention);

	}

	@Test
	void 다중_플그_멘션을_크루_멘션으로_변환합니다() {
		String orginalCrewMention = "안녕하세요-~!@#@김효준[9999999]%^&*+-~!@#@이동훈[1234123412]%^&*+안녕하세요";
		String changeMention = "안녕하세요@김효준[9999999]@이동훈[1234123412]안녕하세요";

		Assertions.assertThat(MemberMentionConvertUtils.convertMentionFormatToCrew(changeMention))
			.isEqualTo(orginalCrewMention);

	}
}
