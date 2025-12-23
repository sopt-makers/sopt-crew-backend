package org.sopt.makers.crew.main.soptmap.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.sopt.makers.crew.main.entity.soptmap.MapTag;
import org.sopt.makers.crew.main.entity.soptmap.SoptMap;
import org.sopt.makers.crew.main.entity.soptmap.repository.SoptMapRepository;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.global.exception.ErrorStatus;
import org.sopt.makers.crew.main.global.exception.ForbiddenException;
import org.sopt.makers.crew.main.global.exception.NotFoundException;
import org.sopt.makers.crew.main.global.pagination.dto.PageMetaDto;
import org.sopt.makers.crew.main.global.pagination.dto.PageOptionsDto;
import org.sopt.makers.crew.main.soptmap.dto.SortType;
import org.sopt.makers.crew.main.soptmap.dto.response.SoptMapGetAllDto;
import org.sopt.makers.crew.main.soptmap.dto.response.SoptMapListResponseDto;
import org.sopt.makers.crew.main.soptmap.service.dto.CreateSoptMapDto;
import org.sopt.makers.crew.main.soptmap.service.dto.SoptMapWithRecommendInfo;
import org.sopt.makers.crew.main.soptmap.service.dto.SubwayStationDto;
import org.sopt.makers.crew.main.soptmap.service.dto.UpdateSoptMapDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
		SoptMap soptMap = findSoptMapById(soptMapId);
		validateOwnership(soptMap, currentUserId);

		if (isPlaceNameChanged(soptMap, dto.getPlaceName())) {
			validatePlaceNameNotDuplicated(dto.getPlaceName());
		}

		List<Long> subwayStationIds = subwayStationManager.retrieveSubwayStationids(dto.getStationNames());
		soptMap.update(dto, subwayStationIds);
		return soptMap.getId();
	}

	@Transactional(readOnly = true)
	public SoptMapGetAllDto getSoptMapList(
		Integer userId,
		MapTag category,
		SortType sortType,
		PageOptionsDto pageOptionsDto) {
		// 1. PageOptionsDto -> Pageable 변환
		Pageable pageable = PageRequest.of(pageOptionsDto.getPage() - 1, pageOptionsDto.getTake());

		// 2. Repository에서 SoptMap + 추천 정보 조회
		Page<SoptMapWithRecommendInfo> soptMapPage = soptMapRepository.searchSoptMap(
			convertToLongOrNull(userId),
			category,
			sortType,
			pageable);

		// 3. 페이지 메타 정보 생성
		PageMetaDto pageMetaDto = new PageMetaDto(pageOptionsDto, (int)soptMapPage.getTotalElements());

		if (soptMapPage.isEmpty()) {
			return SoptMapGetAllDto.of(List.of(), pageMetaDto);
		}

		// 4. 지하철역 정보 일괄 조회 (N+1 방지)
		Map<Long, String> stationIdToNameMap = fetchStationNameMap(soptMapPage.getContent());

		// 5. DTO 변환
		List<SoptMapListResponseDto> content = convertToListDtos(soptMapPage.getContent(), stationIdToNameMap);

		return SoptMapGetAllDto.of(content, pageMetaDto);
	}

	@Transactional(readOnly = true)
	public List<SubwayStationDto> findSubwayStations(String keyword) {
		return subwayStationManager.findByKeywords(keyword);
	}

	private SoptMap findSoptMapById(Long soptMapId) {
		return soptMapRepository.findById(soptMapId)
			.orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_SOPT_MAP.getErrorCode()));
	}

	private void validatePlaceNameNotDuplicated(String placeName) {
		if (soptMapRepository.existsByPlaceName(placeName)) {
			throw new BadRequestException(ErrorStatus.DUPLICATE_SOPT_MAP_PLACE.getErrorCode());
		}
	}

	private void validateOwnership(SoptMap soptMap, Integer currentUserId) {
		if (!soptMap.getCreatorId().equals(currentUserId.longValue())) {
			throw new ForbiddenException(ErrorStatus.FORBIDDEN_SOPT_MAP_UPDATE.getErrorCode());
		}
	}

	private boolean isPlaceNameChanged(SoptMap soptMap, String newPlaceName) {
		return !soptMap.getPlaceName().equals(newPlaceName);
	}

	private Long convertToLongOrNull(Integer userId) {
		if (userId == null) {
			return null;
		}
		return userId.longValue();
	}

	private Set<Long> extractAllStationIds(List<SoptMapWithRecommendInfo> soptMaps) {
		return soptMaps.stream()
			.flatMap(info -> {
				if (info.getNearbyStationIds() == null) {
					return Stream.empty();
				}
				return info.getNearbyStationIds().stream();
			})
			.collect(Collectors.toSet());
	}

	private Map<Long, String> fetchStationNameMap(List<SoptMapWithRecommendInfo> soptMaps) {
		Set<Long> allStationIds = extractAllStationIds(soptMaps);

		if (allStationIds.isEmpty()) {
			return Map.of();
		}

		return subwayStationManager.findByIds(List.copyOf(allStationIds));
	}

	private List<SoptMapListResponseDto> convertToListDtos(
		List<SoptMapWithRecommendInfo> soptMaps,
		Map<Long, String> stationIdToNameMap) {
		return soptMaps.stream()
			.map(info -> convertToListDto(info, stationIdToNameMap))
			.toList();
	}

	private SoptMapListResponseDto convertToListDto(
		SoptMapWithRecommendInfo info,
		Map<Long, String> stationIdToNameMap) {
		List<String> subwayStationNames = mapStationIdsToNames(info.getNearbyStationIds(), stationIdToNameMap);

		return SoptMapListResponseDto.builder()
			.id(info.getId())
			.placeName(info.getPlaceName())
			.description(info.getDescription())
			.mapTags(info.getMapTags())
			.subwayStationNames(subwayStationNames)
			.recommendCount(info.getRecommendCount())
			.isRecommended(info.getIsRecommended())
			.build();
	}

	private List<String> mapStationIdsToNames(List<Long> stationIds, Map<Long, String> stationIdToNameMap) {
		if (stationIds == null) {
			return List.of();
		}

		return stationIds.stream()
			.map(stationIdToNameMap::get)
			.filter(Objects::nonNull) // 존재하지 않는 역 ID 제외
			.toList();
	}
}
