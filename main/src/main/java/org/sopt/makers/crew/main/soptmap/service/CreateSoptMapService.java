package org.sopt.makers.crew.main.soptmap.service;

import java.util.List;

import org.sopt.makers.crew.main.entity.soptmap.SoptMap;
import org.sopt.makers.crew.main.entity.soptmap.repository.SoptMapRepository;
import org.sopt.makers.crew.main.soptmap.dto.CreateSoptMapDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateSoptMapService {

	private final SoptMapRepository soptMapRepository;

	private final SubwayStationManager subwayStationManager;

	/**
	 * 장소 이름 unique, 주변 지하철역
	 */
	@Transactional
	public Long createSoptMap(Integer userId, CreateSoptMapDto dto) {
		List<Long> subwayStationIds = subwayStationManager.retrieveSubwayStationids(dto.getStationNames());
		if (soptMapRepository.existsByPlaceName(dto.getPlaceName()))
			throw new IllegalArgumentException("Place already exists");
		SoptMap save = soptMapRepository.save(SoptMap.create(userId, dto, subwayStationIds));
		return save.getId();
	}

}
