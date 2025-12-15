package org.sopt.makers.crew.main.soptmap.service;

import java.util.List;

import org.sopt.makers.crew.main.entity.soptmap.SoptMap;
import org.sopt.makers.crew.main.entity.soptmap.repository.SoptMapRepository;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.global.exception.ErrorStatus;
import org.sopt.makers.crew.main.global.exception.ForbiddenException;
import org.sopt.makers.crew.main.global.exception.NotFoundException;
import org.sopt.makers.crew.main.soptmap.service.dto.CreateSoptMapDto;
import org.sopt.makers.crew.main.soptmap.service.dto.SubwayStationDto;
import org.sopt.makers.crew.main.soptmap.service.dto.UpdateSoptMapDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SoptMapService {
	private final SoptMapRepository soptMapRepository;
	private final SubwayStationManager subwayStationManager;

	@Transactional
	public Long createSoptMap(Integer userId, CreateSoptMapDto dto) {
		validatePlaceNameNotDuplicated(dto.getPlaceName());

		List<Long> subwayStationIds = subwayStationManager.retrieveSubwayStationids(dto.getStationNames());

		SoptMap soptMap = SoptMap.create(userId, dto, subwayStationIds);
		return soptMapRepository.save(soptMap).getId();
	}

	@Transactional
	public Long updateSoptMap(Integer currentUserId, Long soptMapId, UpdateSoptMapDto dto) {
		SoptMap soptMap = soptMapRepository.findById(soptMapId)
			.orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_SOPT_MAP.getErrorCode()));

		validateOwnership(soptMap, currentUserId);

		if (!soptMap.getPlaceName().equals(dto.getPlaceName())) {
			validatePlaceNameNotDuplicated(dto.getPlaceName());
		}

		List<Long> subwayStationIds = subwayStationManager.retrieveSubwayStationids(dto.getStationNames());

		soptMap.update(dto, subwayStationIds);
		return soptMap.getId();
	}

	private void validatePlaceNameNotDuplicated(String placeName) {
		if (soptMapRepository.existsByPlaceName(placeName)) {
			throw new BadRequestException(ErrorStatus.DUPLICATE_SOPT_MAP_PLACE.getErrorCode());
		}
	}

	private void validateOwnership(SoptMap soptMap, Integer currentUserId) {
		if (soptMap.getCreatorId() != currentUserId.longValue()) {
			throw new ForbiddenException(ErrorStatus.FORBIDDEN_SOPT_MAP_UPDATE.getErrorCode());
		}
	}

	public List<SubwayStationDto> findSubwayStations(String keyword) {
		return subwayStationManager.findByKeywords(keyword);
	}
}
