package org.sopt.makers.crew.main.soptmap.service;

import java.util.List;

import org.sopt.makers.crew.main.entity.soptmap.repository.SubwayStationRepository;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.global.exception.ErrorStatus;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SubwayStationManager {

	private final SubwayStationRepository subwayStationRepository;

	public List<Long> retrieveSubwayStationids(List<String> subwayStationNames) {
		List<Long> subwayStationsIds = subwayStationRepository.findIdsByStationNames(subwayStationNames);

		if (subwayStationsIds.size() != subwayStationNames.size()) {
			throw new BadRequestException(ErrorStatus.NOT_FOUND_SUBWAY_STATION.getErrorCode());
		}

		return subwayStationsIds;
	}

}
