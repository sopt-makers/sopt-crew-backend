package org.sopt.makers.crew.main.entity.advertisement;

import static org.assertj.core.api.Assertions.*;
import static org.sopt.makers.crew.main.entity.advertisement.enums.AdvertisementCategory.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sopt.makers.crew.main.entity.advertisement.enums.EventType;
import org.sopt.makers.crew.main.entity.advertisement.enums.TargetGeneration;

class AdvertisementTest {

	@Test
	@DisplayName("광고 생성 시 isDisplay를 지정하지 않으면 기본값은 true다.")
	void advertisementBuilder_defaultsDisplayToTrue() {
		Advertisement advertisement = Advertisement.builder()
			.advertisementDesktopImageUrl("https://example.com/desktop.png")
			.advertisementMobileImageUrl("https://example.com/mobile.png")
			.advertisementLink("https://example.com")
			.advertisementCategory(MEETING_TOP)
			.priority(1L)
			.advertisementStartDate(LocalDateTime.of(2026, 5, 1, 0, 0))
			.advertisementEndDate(LocalDateTime.of(2026, 5, 10, 0, 0))
			.isSponsoredContent(false)
			.eventType(EventType.SOPKATHON)
			.targetGeneration(TargetGeneration.ALL)
			.build();

		assertThat(advertisement.isDisplay()).isTrue();
	}

	@Test
	@DisplayName("광고 링크는 null일 수 있다.")
	void advertisementBuilder_allowsNullAdvertisementLink() {
		Advertisement advertisement = Advertisement.builder()
			.advertisementDesktopImageUrl("https://example.com/desktop.png")
			.advertisementMobileImageUrl("https://example.com/mobile.png")
			.advertisementLink(null)
			.advertisementCategory(MEETING_TOP)
			.priority(1L)
			.advertisementStartDate(LocalDateTime.of(2026, 5, 1, 0, 0))
			.advertisementEndDate(LocalDateTime.of(2026, 5, 10, 0, 0))
			.isSponsoredContent(false)
			.eventType(EventType.SOPKATHON)
			.targetGeneration(TargetGeneration.ALL)
			.build();

		assertThat(advertisement.getAdvertisementLink()).isNull();
	}
}
