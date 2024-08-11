package org.sopt.makers.crew.main.advertisement.service;

import java.util.List;
import java.util.Optional;

import org.sopt.makers.crew.main.advertisement.dto.AdvertisementGetResponseDto;
import org.sopt.makers.crew.main.common.util.Time;
import org.sopt.makers.crew.main.entity.advertisement.Advertisement;
import org.sopt.makers.crew.main.entity.advertisement.AdvertisementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdvertisementService {
	private final AdvertisementRepository advertisementRepository;

	private final Time time;

	public AdvertisementGetResponseDto getAdvertisement() {
		Optional<Advertisement> advertisement = advertisementRepository.findFirstByAdvertisementEndDateBeforeAndAdvertisementStartDateAfterOrderByPriority(
			time.now(), time.now());

		return advertisement
			.map(AdvertisementGetResponseDto::of)
			.orElseGet(() -> AdvertisementGetResponseDto.of(null));
	}
}
