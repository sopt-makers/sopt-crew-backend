package org.sopt.makers.crew.main.soptmap.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.sopt.makers.crew.main.admin.v2.service.AdminService;
import org.sopt.makers.crew.main.entity.property.Property;
import org.sopt.makers.crew.main.entity.property.PropertyKeys;
import org.sopt.makers.crew.main.entity.soptmap.MapTag;
import org.sopt.makers.crew.main.entity.soptmap.SoptMap;
import org.sopt.makers.crew.main.entity.soptmap.SubwayStation;
import org.sopt.makers.crew.main.entity.soptmap.repository.EventGiftRepository;
import org.sopt.makers.crew.main.entity.soptmap.repository.SoptMapRepository;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.global.exception.ErrorStatus;
import org.sopt.makers.crew.main.global.exception.ForbiddenException;
import org.sopt.makers.crew.main.global.exception.NotFoundException;
import org.sopt.makers.crew.main.global.exception.ServerException;
import org.sopt.makers.crew.main.global.pagination.dto.PageMetaDto;
import org.sopt.makers.crew.main.global.pagination.dto.PageOptionsDto;
import org.sopt.makers.crew.main.global.util.DataConverter;
import org.sopt.makers.crew.main.global.util.DataUtils;
import org.sopt.makers.crew.main.global.util.DateUtils;
import org.sopt.makers.crew.main.soptmap.dto.CreateSoptMapResponseDto;
import org.sopt.makers.crew.main.soptmap.dto.SortType;
import org.sopt.makers.crew.main.soptmap.dto.ToggleSoptMapRecommendDto;
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

import com.fasterxml.jackson.core.type.TypeReference;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SoptMapService {

	private static final String EVENT_DATE_KEY = "eventDate";
	private final SoptMapRepository soptMapRepository;
	private final MapRecommendManager mapRecommendManager;
	private final SubwayStationManager subwayStationManager;
	private final AdminService adminService;
	private final DataConverter dataConverter;
	private final EventGiftRepository eventGiftRepository;

	@Transactional
	public CreateSoptMapResponseDto createSoptMap(Integer userId, CreateSoptMapDto dto) {
		validatePlaceNameNotDuplicated(dto.getPlaceName());

		boolean isFirstRegistration = !soptMapRepository.existsByCreatorId(Long.valueOf(userId));
		List<Long> subwayStationIds = subwayStationManager.retrieveSubwayStationids(dto.getStationNames());

		SoptMap soptMap = SoptMap.create(userId, dto, subwayStationIds);
		Long soptMapId = soptMapRepository.save(soptMap).getId();

		return CreateSoptMapResponseDto.of(soptMapId, isFirstRegistration);
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

	@Transactional
	public void deleteSoptMap(Integer userId, Long soptMapId) {
		SoptMap soptMap = findSoptMapById(soptMapId);
		validateOwnership(soptMap, userId);

		mapRecommendManager.deleteAllBySoptMapId(soptMapId);
		soptMapRepository.delete(soptMap);
	}

	@Transactional(readOnly = true)
	public SoptMapGetAllDto getSoptMapList(
		Integer userId,
		List<MapTag> categories,
		SortType sortType,
		String stationKeyword,
		PageOptionsDto pageOptionsDto) {

		Pageable pageable = createPageable(pageOptionsDto);
		List<Long> stationIds = resolveStationIds(stationKeyword);

		if (isEmptySearchResult(stationKeyword, stationIds)) {
			return createEmptyResult(pageOptionsDto);
		}

		Page<SoptMapWithRecommendInfo> soptMapPage = fetchSoptMaps(userId, categories, sortType, stationIds, pageable);

		if (soptMapPage.isEmpty()) {
			return createEmptyResult(pageOptionsDto, (int)soptMapPage.getTotalElements());
		}

		return buildResultDto(soptMapPage, pageOptionsDto, userId);
	}

	@Transactional(readOnly = true)
	public List<SubwayStationDto> findSubwayStations(String keyword) {
		return subwayStationManager.findByKeywords(keyword);
	}

	private Pageable createPageable(PageOptionsDto pageOptionsDto) {
		return PageRequest.of(pageOptionsDto.getPage() - 1, pageOptionsDto.getTake());
	}

	private List<Long> resolveStationIds(String stationKeyword) {
		if (stationKeyword == null || stationKeyword.isBlank()) {
			return List.of();
		}

		List<SubwayStation> stations = subwayStationManager.searchByKeyword(stationKeyword);

		if (stations.isEmpty()) {
			return List.of();
		}

		return stations.stream()
			.map(SubwayStation::getId)
			.toList();
	}

	private boolean isEmptySearchResult(String stationKeyword, List<Long> stationIds) {
		// 키워드가 있는데 검색 결과가 없는 경우만 true
		return stationKeyword != null && !stationKeyword.isBlank() && stationIds.isEmpty();
	}

	private Page<SoptMapWithRecommendInfo> fetchSoptMaps(
		Integer userId,
		List<MapTag> categories,
		SortType sortType,
		List<Long> stationIds,
		Pageable pageable) {

		List<Long> filterIds = stationIds;
		if (stationIds != null && stationIds.isEmpty()) {
			filterIds = null;
		}

		return soptMapRepository.searchSoptMap(
			convertToLongOrNull(userId),
			categories,
			sortType,
			filterIds,
			pageable);
	}

	private SoptMapGetAllDto buildResultDto(Page<SoptMapWithRecommendInfo> soptMapPage, PageOptionsDto pageOptionsDto,
		Integer userId) {
		PageMetaDto pageMetaDto = new PageMetaDto(pageOptionsDto, (int)soptMapPage.getTotalElements());
		Map<Long, String> stationIdToNameMap = fetchStationNameMap(soptMapPage.getContent());
		List<SoptMapListResponseDto> content = convertToListDtos(soptMapPage.getContent(), stationIdToNameMap,
			userId);

		return SoptMapGetAllDto.of(content, pageMetaDto);
	}

	private SoptMapGetAllDto createEmptyResult(PageOptionsDto pageOptionsDto) {
		return createEmptyResult(pageOptionsDto, 0);
	}

	private SoptMapGetAllDto createEmptyResult(PageOptionsDto pageOptionsDto, int totalCount) {
		PageMetaDto emptyMeta = new PageMetaDto(pageOptionsDto, totalCount);
		return SoptMapGetAllDto.of(List.of(), emptyMeta);
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
		Map<Long, String> stationIdToNameMap, Integer userId) {
		return soptMaps.stream()
			.map(info -> convertToListDto(info, stationIdToNameMap, userId))
			.toList();
	}

	private SoptMapListResponseDto convertToListDto(
		SoptMapWithRecommendInfo info,
		Map<Long, String> stationIdToNameMap, Integer userId) {
		List<String> subwayStationNames = mapStationIdsToNames(info.getNearbyStationIds(), stationIdToNameMap);

		User retrievedUser = info.getUser();
		String registeredName = DataUtils.safeData(retrievedUser, User::getName);
		Integer creatorId = DataUtils.safeData(retrievedUser, User::getId);

		boolean registeredByMe = creatorId != null &&
			userId != null &&
			creatorId.longValue() == userId;

		return SoptMapListResponseDto.builder()
			.id(info.getId())
			.placeName(info.getPlaceName())
			.description(info.getDescription())
			.mapTags(info.getMapTags())
			.subwayStationNames(subwayStationNames)
			.recommendCount(info.getRecommendCount())
			.isRecommended(info.getIsRecommended())
			.kakaoLink(info.getKakaoLink())
			.naverLink(info.getNaverLink())
			.creatorName(registeredName)
			.isCreator(registeredByMe)
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

	public ToggleSoptMapRecommendDto toggleRecommendMap(Integer userId, Long soptMapId) {
		if (!soptMapRepository.existsById(soptMapId)) {
			throw new BadRequestException("잘못된 솝맵 지도 Id 입니다.");
		}

		return mapRecommendManager.toggleRecommend(Long.valueOf(userId), soptMapId);
	}

	@Transactional
	public boolean checkEventWinning(Integer userId, Long soptMapId) {

		Property eventDateProperty = adminService.findPropertyByKey(PropertyKeys.SOPT_MAP_EVENT.getKey());

		Map<String, Object> properties = eventDateProperty.getProperties();

		String from = String.valueOf(properties.get(PropertyKeys.START_DATE.getKey()));
		String to = String.valueOf(properties.get(PropertyKeys.END_DATE.getKey()));
		if (from == null || to == null) {
			throw new ServerException("이벤트 날짜 범위가 지정되어있지 않습니다 -> 어드민에서 설정해 주세요");
		}

		LocalDate startDate = DateUtils.toLocalDate(from);
		LocalDate endDate = DateUtils.toLocalDate(to);

		List<SoptMap> firstEventSoptMaps = soptMapRepository.findFirstEventSoptMaps(startDate.atStartOfDay(),
			endDate.atStartOfDay());

		List<Integer> eventNumbers = dataConverter.convertValue(properties.get(PropertyKeys.EVENT_NUM.getKey()),
			new TypeReference<>() {
			}
		);

		List<Integer> eventIdx = eventNumbers.stream()
			.map(m -> m - 1)
			.toList();

		int findSoptMapSize = firstEventSoptMaps.size();

		boolean isWinnerAvailable = eventIdx.stream()
			.filter(idx -> idx < findSoptMapSize)
			.anyMatch(idx -> firstEventSoptMaps.get(idx).getId().equals(soptMapId) && firstEventSoptMaps.get(idx)
				.getCreatorId()
				.equals(Long.valueOf(userId)));

		if (isWinnerAvailable) {
			return assignGift(userId, soptMapId);
		}
		return false;
	}

	private boolean assignGift(Integer userId, Long soptMapId) {

		if (eventGiftRepository.existsByUserId(userId))
			return false;

		return eventGiftRepository.findFirstClaimableGiftWithLock()
			.map(gift -> {
				gift.claimGift(userId, soptMapId);
				eventGiftRepository.save(gift);
				return true;
			})
			.orElseGet(() -> {
				log.warn("선물이 모두 소진되었습니다. userId: {}, soptMapId: {}", userId, soptMapId);
				return false;
			});
	}
}
