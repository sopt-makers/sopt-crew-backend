package org.sopt.makers.crew.main.entity.meeting.enums;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MeetingJoinablePartTest {

	@Test
	@DisplayName("MeetingJoinablePart는 고정된 표시 이름을 가진다.")
	void meetingJoinablePart_hasDisplayName() {
		assertThat(MeetingJoinablePart.PM.getDisplayName()).isEqualTo("기획");
		assertThat(MeetingJoinablePart.DESIGN.getDisplayName()).isEqualTo("디자인");
		assertThat(MeetingJoinablePart.IOS.getDisplayName()).isEqualTo("iOS");
		assertThat(MeetingJoinablePart.ANDROID.getDisplayName()).isEqualTo("안드로이드");
		assertThat(MeetingJoinablePart.SERVER.getDisplayName()).isEqualTo("서버");
		assertThat(MeetingJoinablePart.WEB.getDisplayName()).isEqualTo("웹");
	}
}
