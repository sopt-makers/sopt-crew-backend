package org.sopt.makers.crew.main.advertisement.service;

import java.util.List;

import org.sopt.makers.crew.main.advertisement.dto.AdvertisementGetDto;
import org.sopt.makers.crew.main.advertisement.dto.AdvertisementsGetResponseDto;
import org.sopt.makers.crew.main.common.util.Time;
import org.sopt.makers.crew.main.entity.advertisement.Advertisement;
import org.sopt.makers.crew.main.entity.advertisement.AdvertisementRepository;
import org.sopt.makers.crew.main.entity.advertisement.enums.AdvertisementCategory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdvertisementService {
	private final AdvertisementRepository advertisementRepository;

	private final Time time;

	public AdvertisementsGetResponseDto getAdvertisement(AdvertisementCategory advertisementCategory) {
		List<Advertisement> advertisements = advertisementRepository
			.findTop6ByAdvertisementCategoryAndAdvertisementEndDateAfterAndAdvertisementStartDateBeforeOrderByPriority(
			advertisementCategory, time.now(), time.now());

		if (advertisements.isEmpty()) {
			return AdvertisementsGetResponseDto.of(null);
		}

		List<AdvertisementGetDto> advertisementDtos = advertisements.stream().map(AdvertisementGetDto::of)
			.toList();

		return AdvertisementsGetResponseDto.of(advertisementDtos);
	}
}
