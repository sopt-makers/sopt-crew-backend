package org.sopt.makers.crew.main.advertisement.service;

import java.time.LocalDateTime;
import java.util.List;

import org.sopt.makers.crew.main.advertisement.dto.AdvertisementsGetResponseDto;
import org.sopt.makers.crew.main.advertisement.dto.AdvertisementsGetResponseDto.AdvertisementGetDto;
import org.sopt.makers.crew.main.common.util.Time;
import org.sopt.makers.crew.main.entity.advertisement.Advertisement;
import org.sopt.makers.crew.main.entity.advertisement.AdvertisementRepository;
import org.sopt.makers.crew.main.entity.advertisement.enums.AdvertisementCategory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
		LocalDateTime now = time.now();

		int maxItems = advertisementCategory.getMaxItems();
		Pageable pageable = PageRequest.of(0, maxItems);

		List<Advertisement> advertisements = advertisementRepository.findAdvertisementsByDateAndType(
			advertisementCategory,
			true,
			now,
			pageable);

		if (!advertisements.isEmpty()) {
			return createResponseDto(advertisements);
		}

		advertisements = advertisementRepository.findAdvertisementsByCategory(
			advertisementCategory,
			false,
			pageable);

		return createResponseDto(advertisements);
	}

	private AdvertisementsGetResponseDto createResponseDto(List<Advertisement> advertisements) {
		List<AdvertisementGetDto> advertisementDtos = advertisements.stream()
			.map(AdvertisementGetDto::of)
			.toList();
		return AdvertisementsGetResponseDto.of(advertisementDtos);
	}
}