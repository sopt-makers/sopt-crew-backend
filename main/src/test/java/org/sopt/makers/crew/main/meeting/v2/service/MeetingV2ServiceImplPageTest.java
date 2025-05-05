package org.sopt.makers.crew.main.meeting.v2.service;

import java.util.concurrent.atomic.AtomicInteger;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MeetingV2ServiceImplPageTest {

	@Test
	@DisplayName("첫 페이지 테스트 넘버링 테스트")
	void testPage() {

		int getPage = 1;
		int getTake = 10;

		int startPage = (getPage - 1) * getTake;

		AtomicInteger applyNumbers = new AtomicInteger(startPage + 1);

		Assertions.assertThat(applyNumbers.getAndIncrement()).isEqualTo(1);
		Assertions.assertThat(applyNumbers.get()).isEqualTo(2);
	}

	@Test
	@DisplayName("사이에 있는 페이지 넘버링 테스트")
	void testPage2() {

		int getPage = 3;
		int getTake = 10;

		int startPage = (getPage - 1) * getTake;

		AtomicInteger applyNumbers = new AtomicInteger(startPage + 1);

		Assertions.assertThat(applyNumbers.getAndIncrement()).isEqualTo(21);
		Assertions.assertThat(applyNumbers.get()).isEqualTo(22);
	}

}