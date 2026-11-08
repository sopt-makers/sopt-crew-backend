package org.sopt.makers.crew.main.dining.v2.service;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;

/** prod 회원 activities.part 실제 값 기준 (2026.10) */
class DiningUserPartResolverTest {

	@ParameterizedTest(name = "{0} -> {1}")
	@CsvSource({
		"기획, PM", "기획 파트장, PM", "PM, PM",
		"디자인, DESIGN", "디자인 파트장, DESIGN",
		"iOS, IOS", "ios, IOS", "iOS 파트장, IOS",
		"안드로이드, ANDROID", "안드로이드 파트장, ANDROID",
		"서버, SERVER", "서버 파트장, SERVER", "서버파트장, SERVER", "백엔드, SERVER",
		"웹, WEB", "웹 파트장, WEB", "프론트엔드, WEB"
	})
	@DisplayName("파트 표기, 파트장 표기, 대소문자, 별칭을 회식 참여 파트로 변환한다.")
	void resolvesPart(String part, MeetingJoinablePart expected) {
		assertThat(DiningUserPartResolver.resolve(part)).contains(expected);
	}

	@ParameterizedTest
	@ValueSource(strings = {"총무", "회장", "부회장", "CX", "메이커스 리드", "메이커스 팀장", "미디어 팀장", "운영 팀장",
		"리서처", "오거나이저", "마케터", ""})
	@NullSource
	@DisplayName("파트가 아닌 역할 값은 변환하지 않는다.")
	void ignoresRoles(String part) {
		assertThat(DiningUserPartResolver.resolve(part)).isEmpty();
	}
}
