package org.sopt.makers.crew.main.soptmap.service;

import java.util.List;

import org.sopt.makers.crew.main.entity.soptmap.SubwayStation;
import org.sopt.makers.crew.main.entity.soptmap.repository.SubwayStationRepository;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.global.exception.ErrorStatus;
import org.sopt.makers.crew.main.soptmap.service.dto.SubwayStationDto;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SubwayStationManager {

	private final SubwayStationRepository subwayStationRepository;

	public List<Long> retrieveSubwayStationids(List<String> subwayStationNames) {
		if (subwayStationNames == null || subwayStationNames.isEmpty()) {
			return List.of();
		}

		List<String> uniqueStationNames = getUniqueStationNames(subwayStationNames);
		List<SubwayStation> stations = subwayStationRepository.findAllByNameIn(uniqueStationNames);

		validateAllStationsFound(uniqueStationNames, stations);

		return extractStationIds(stations);
	}

	private List<String> getUniqueStationNames(List<String> subwayStationNames) {
		return subwayStationNames.stream()
			.distinct()
			.toList();
	}

	private void validateAllStationsFound(List<String> uniqueStationNames, List<SubwayStation> stations) {
		if (stations.size() != uniqueStationNames.size()) {
			List<String> foundNames = stations.stream()
				.map(SubwayStation::getName)
				.toList();

			List<String> missingNames = uniqueStationNames.stream()
				.filter(name -> !foundNames.contains(name))
				.toList();

			throw new BadRequestException(ErrorStatus.NOT_FOUND_SUBWAY_STATION.getErrorCode() + " : " + missingNames);
		}
	}

	private List<Long> extractStationIds(List<SubwayStation> stations) {
		return stations.stream()
			.map(SubwayStation::getId)
			.toList();
	}

	public List<SubwayStationDto> findByKeywords(String keyword) {
		return subwayStationRepository.searchByKeyword(keyword)
			.stream().map(SubwayStation::toDto).toList();
	}
}
