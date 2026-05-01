package org.sopt.makers.crew.main.advertisement.service;

import static org.sopt.makers.crew.main.entity.advertisement.enums.AdvertisementCategory.*;

import org.sopt.makers.crew.main.entity.advertisement.Advertisement;
import org.sopt.makers.crew.main.entity.advertisement.AdvertisementRepository;
import org.sopt.makers.crew.main.entity.advertisement.enums.AdvertisementCategory;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AdvertisementValidator {

	private final AdvertisementRepository advertisementRepository;

	public void validateGeneralAdvertisementCategory(AdvertisementCategory advertisementCategory) {
		if (!advertisementCategory.isGeneralAdvertisement()) {
			throw new BadRequestException("일반 광고 조회에서 허용하지 않는 카테고리입니다.");
		}
	}

	public void validateMeetingTopAdvertisement(Advertisement advertisement) {
		if (advertisement.getAdvertisementCategory() != MEETING_TOP) {
			throw new BadRequestException("모임 상단 광고만 노출 여부를 수정할 수 있습니다.");
		}
	}

	public void validateSingleMeetingTopDisplay(Advertisement advertisement, boolean isDisplay) {
		if (!shouldTurnOn(advertisement, isDisplay)) {
			return;
		}

		boolean existsOtherDisplayedAdvertisement = advertisementRepository.existsDisplayedAdvertisementByCategoryExcludingId(
			MEETING_TOP,
			advertisement.getId());
		if (existsOtherDisplayedAdvertisement) {
			throw new BadRequestException("모임 상단 광고는 하나만 노출할 수 있습니다.");
		}
	}

	private boolean shouldTurnOn(Advertisement advertisement, boolean isDisplay) {
		return isDisplay && !advertisement.isDisplay();
	}
}
