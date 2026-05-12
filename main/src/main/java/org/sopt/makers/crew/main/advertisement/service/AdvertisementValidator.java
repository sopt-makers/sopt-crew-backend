package org.sopt.makers.crew.main.advertisement.service;

import static org.sopt.makers.crew.main.entity.advertisement.enums.AdvertisementCategory.*;

import java.time.LocalDateTime;

import org.sopt.makers.crew.main.admin.v2.dto.AdvertisementMeetingTopUpdateRequest;
import org.sopt.makers.crew.main.entity.advertisement.Advertisement;
import org.sopt.makers.crew.main.entity.advertisement.AdvertisementRepository;
import org.sopt.makers.crew.main.entity.advertisement.enums.AdvertisementCategory;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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

	public void validateMeetingTopUpdateRequest(Advertisement advertisement,
		AdvertisementMeetingTopUpdateRequest request) {
		if (request == null) {
			throw new BadRequestException("수정할 모임 상단 광고 필드가 없습니다.");
		}
		if (!request.hasUpdateField()) {
			throw new BadRequestException("수정할 모임 상단 광고 필드가 없습니다.");
		}

		validateImageUrl(request.desktopImageUrl(), "데스크톱 이미지 URL");
		validateImageUrl(request.mobileImageUrl(), "모바일 이미지 URL");
		validateImageUrl(request.calendarImageUrl(), "달력 이미지 URL");
		validateNotBlank(request.titlePrefix(), "제목 prefix");
		validateNotBlank(request.titleHighlight(), "제목 highlight");
		validateNotBlank(request.titleSuffix(), "제목 suffix");
		validateNotBlank(request.subTitle(), "부제목");
		validateAdvertisementPeriod(advertisement, request);
	}

	private void validateImageUrl(String imageUrl, String fieldName) {
		validateNotBlank(imageUrl, fieldName);
	}

	private void validateNotBlank(String value, String fieldName) {
		if (value != null && !StringUtils.hasText(value)) {
			throw new BadRequestException(fieldName + "은 비워둘 수 없습니다.");
		}
	}

	private void validateAdvertisementPeriod(Advertisement advertisement,
		AdvertisementMeetingTopUpdateRequest request) {
		LocalDateTime startDate = request.advertisementStartDate() == null
			? advertisement.getAdvertisementStartDate()
			: request.advertisementStartDate();
		LocalDateTime endDate = request.advertisementEndDate() == null
			? advertisement.getAdvertisementEndDate()
			: request.advertisementEndDate();

		if (startDate.isAfter(endDate)) {
			throw new BadRequestException("광고 시작일은 종료일보다 이후일 수 없습니다.");
		}
	}
}
