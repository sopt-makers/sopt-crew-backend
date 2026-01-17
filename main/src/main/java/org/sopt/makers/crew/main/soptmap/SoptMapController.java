package org.sopt.makers.crew.main.soptmap;

import static org.sopt.makers.crew.main.soptmap.dto.request.SoptMapRequest.CreateSoptMapRequest;
import static org.sopt.makers.crew.main.soptmap.dto.request.SoptMapRequest.SoptMapUpdateRequest;
import static org.sopt.makers.crew.main.soptmap.dto.response.SoptMapResponse.CreateSoptMapResponse;
import static org.sopt.makers.crew.main.soptmap.dto.response.SoptMapResponse.SearchSubwayStationResponse;
import static org.sopt.makers.crew.main.soptmap.dto.response.SoptMapResponse.SoptMapEventResponse;
import static org.sopt.makers.crew.main.soptmap.dto.response.SoptMapResponse.SoptMapGiftResponse;
import static org.sopt.makers.crew.main.soptmap.dto.response.SoptMapResponse.ToggleSoptMapResponse;

import java.net.URI;
import java.security.Principal;
import java.util.List;

import org.sopt.makers.crew.main.entity.soptmap.MapTag;
import org.sopt.makers.crew.main.global.pagination.dto.PageOptionsDto;
import org.sopt.makers.crew.main.global.util.UserUtil;
import org.sopt.makers.crew.main.soptmap.dto.CreateSoptMapResponseDto;
import org.sopt.makers.crew.main.soptmap.dto.SortType;
import org.sopt.makers.crew.main.soptmap.dto.response.SoptMapDetailResponseDto;
import org.sopt.makers.crew.main.soptmap.dto.response.SoptMapGetAllDto;
import org.sopt.makers.crew.main.soptmap.service.EventGiftService;
import org.sopt.makers.crew.main.soptmap.service.SoptMapRequestValidator;
import org.sopt.makers.crew.main.soptmap.service.SoptMapService;
import org.sopt.makers.crew.main.soptmap.service.dto.EventGiftDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/map")
public class SoptMapController implements SoptMapApi {

	private final SoptMapRequestValidator soptMapRequestValidator;
	private final SoptMapService soptMapService;
	private final EventGiftService eventGiftService;

	@Override
	@PostMapping
	public ResponseEntity<CreateSoptMapResponse> createSoptMap(Principal principal,
		@RequestBody @Valid CreateSoptMapRequest request) {
		soptMapRequestValidator.validate(request);

		CreateSoptMapResponseDto dto = soptMapService.createSoptMap(UserUtil.getUserId(principal), request.toDto());
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
			.path("/{id}")
			.buildAndExpand(dto.soptMapId())
			.toUri();
		return ResponseEntity.created(location).body(CreateSoptMapResponse.from(dto));
	}

	@Override
	@PutMapping("/{soptMapId}")
	public ResponseEntity<CreateSoptMapResponse> updateSoptMap(Principal principal,
		@PathVariable Long soptMapId,
		@RequestBody @Valid SoptMapUpdateRequest request) {
		soptMapRequestValidator.validate(request);

		Long updatedId = soptMapService.updateSoptMap(
			UserUtil.getUserId(principal),
			soptMapId,
			request.toDto());
		return ResponseEntity.ok(CreateSoptMapResponse.from(updatedId));
	}

	@Override
	@DeleteMapping("/{soptMapId}")
	public ResponseEntity<Void> deleteSoptMap(Principal principal, @PathVariable Long soptMapId) {
		soptMapService.deleteSoptMap(UserUtil.getUserId(principal), soptMapId);
		return ResponseEntity.noContent().build();
	}

	@Override
	@GetMapping("/search/subway")
	public ResponseEntity<SearchSubwayStationResponse> findSubwayStations(Principal principal,
		@RequestParam(required = false) String keyword) {
		return ResponseEntity.ok(SearchSubwayStationResponse.from(soptMapService.findSubwayStations(keyword)));
	}

	@Override
	@GetMapping
	public ResponseEntity<SoptMapGetAllDto> getSoptMapList(
		Principal principal,
		@RequestParam(required = false) List<MapTag> categories,
		@RequestParam(defaultValue = "LATEST") SortType sortType,
		@RequestParam(required = false) String stationKeyword,
		@RequestParam(defaultValue = "1") Integer page,
		@RequestParam(defaultValue = "10") Integer take) {
		Integer userId = UserUtil.getUserId(principal);
		PageOptionsDto pageOptionsDto = new PageOptionsDto(page, take);
		SoptMapGetAllDto result = soptMapService.getSoptMapList(
			userId,
			categories,
			sortType,
			stationKeyword,
			pageOptionsDto);
		return ResponseEntity.ok(result);
	}

	@Override
	@GetMapping("/{soptMapId}")
	public ResponseEntity<SoptMapDetailResponseDto> getSoptMapDetail(
		Principal principal,
		@PathVariable Long soptMapId) {
		return ResponseEntity.ok(
			soptMapService.getSoptMapDetail(UserUtil.getUserId(principal), soptMapId));
	}

	@Override
	@PutMapping("/toggle/recommend/{soptMapId}")
	public ResponseEntity<ToggleSoptMapResponse> recommendSoptMap(Principal principal,
		@PathVariable Long soptMapId) {
		Integer userId = UserUtil.getUserId(principal);
		return ResponseEntity.ok(
			ToggleSoptMapResponse.from(soptMapService.toggleRecommendMap(userId, soptMapId)));
	}

	// 이거 보안 상으로 안전하게 보낼 방법이 있나?
	@Override
	@GetMapping("/event/{soptMapId}")
	public ResponseEntity<SoptMapEventResponse> eventSoptMap(
		Principal principal,
		@PathVariable Long soptMapId
	) {
		return ResponseEntity.ok(
			SoptMapEventResponse.from(soptMapService.checkEventWinning(UserUtil.getUserId(principal), soptMapId)));
	}

	@Override
	@GetMapping("/gift/{soptMapId}")
	public ResponseEntity<SoptMapGiftResponse> giftedSoptMap(
		Principal principal,
		@PathVariable Long soptMapId
	) {
		EventGiftDto eventGiftDto = eventGiftService.retrieveEventGift(UserUtil.getUserId(principal), soptMapId);
		return ResponseEntity.ok(
			SoptMapGiftResponse.from(eventGiftDto.getId(), eventGiftDto.getGiftUrl()));
	}

}
