package org.sopt.makers.crew.main.advertisement.service;

import java.util.List;
import java.util.Optional;

import org.sopt.makers.crew.main.advertisement.dto.AdvertisementGetResponseDto;
import org.sopt.makers.crew.main.advertisement.dto.AdvertisementImageDto;
import org.sopt.makers.crew.main.common.util.Time;
import org.sopt.makers.crew.main.entity.advertisement.Advertisement;
import org.sopt.makers.crew.main.entity.advertisement.AdvertisementImage;
import org.sopt.makers.crew.main.entity.advertisement.AdvertisementImageRepository;
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
	private final AdvertisementImageRepository advertisementImageRepository;

	private final Time time;

	public AdvertisementGetResponseDto getAdvertisement(AdvertisementCategory advertisementCategory) {
		Optional<Advertisement> advertisement = advertisementRepository.findFirstByAdvertisementCategoryAndAdvertisementEndDateBeforeAndAdvertisementStartDateAfterOrderByPriority(
			advertisementCategory, time.now(), time.now());

		if (advertisement.isEmpty()) {
			return AdvertisementGetResponseDto.of(null, null);
		}

		List<AdvertisementImage> advertisementImages = advertisementImageRepository.findAllByAdvertisementOrderByImageOrder(
			advertisement.get());

		List<AdvertisementImageDto> imageDtos = advertisementImages.stream()
			.map(AdvertisementImageDto::of)
			.toList();

		return AdvertisementGetResponseDto.of(imageDtos, advertisement.get().getAdvertisementLink());
	}
}
