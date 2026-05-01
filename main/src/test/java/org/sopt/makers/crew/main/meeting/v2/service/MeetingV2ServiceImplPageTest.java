package org.sopt.makers.crew.main.meeting.v2.service;

import java.util.concurrent.atomic.AtomicInteger;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sopt.makers.crew.main.global.pagination.DefaultPageableStrategy;
import org.sopt.makers.crew.main.global.pagination.PageableStrategy;
import org.sopt.makers.crew.main.global.pagination.dto.PageMetaDto;
import org.sopt.makers.crew.main.global.pagination.dto.PageOptionsDto;
import org.springframework.data.domain.Pageable;

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

	@Test
	@DisplayName("빈 결과에서는 page를 1로 정규화하고 이전/다음 페이지를 모두 false로 만든다")
	void normalizeEmptyResultPage() {
		PageableStrategy pageableStrategy = new DefaultPageableStrategy();
		PageOptionsDto pageOptionsDto = new PageOptionsDto(5, 12);

		int normalizedPage = pageableStrategy.normalizePage(pageOptionsDto, 0);
		Pageable pageable = pageableStrategy.createPageable(new PageOptionsDto(normalizedPage, pageOptionsDto.getTake()));
		PageMetaDto pageMetaDto = pageableStrategy.createPageMeta(pageable, 0, pageOptionsDto.getTake());

		Assertions.assertThat(normalizedPage).isEqualTo(1);
		Assertions.assertThat(pageMetaDto.getPage()).isEqualTo(1);
		Assertions.assertThat(pageMetaDto.getPageCount()).isZero();
		Assertions.assertThat(pageMetaDto.isHasPreviousPage()).isFalse();
		Assertions.assertThat(pageMetaDto.isHasNextPage()).isFalse();
	}

}
