package org.sopt.makers.crew.main.soptmap;

import static org.sopt.makers.crew.main.soptmap.dto.request.SoptMapRequest.*;

import java.net.URI;
import java.security.Principal;

import org.sopt.makers.crew.main.entity.soptmap.MapTag;
import org.sopt.makers.crew.main.global.util.UserUtil;
import org.sopt.makers.crew.main.soptmap.dto.SortType;
import org.sopt.makers.crew.main.soptmap.dto.response.SoptMapListResponseDto;
import org.sopt.makers.crew.main.soptmap.dto.response.SoptMapResponse.CreateSoptMapResponse;
import org.sopt.makers.crew.main.soptmap.dto.response.SoptMapResponse.SearchSubwayStationResponse;
import org.sopt.makers.crew.main.soptmap.service.SoptMapRequestValidator;
import org.sopt.makers.crew.main.soptmap.service.SoptMapService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/map")
public class SoptMapController implements SoptMapApi {

	private final SoptMapRequestValidator soptMapRequestValidator;
	private final SoptMapService soptMapService;

	@Override
	@PostMapping
	public ResponseEntity<CreateSoptMapResponse> createSoptMap(Principal principal,
		@RequestBody CreateSoptMapRequest request) {
		soptMapRequestValidator.validate(request);

		Long soptMapId = soptMapService.createSoptMap(UserUtil.getUserId(principal), request.toDto());
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
			.path("/{id}")
			.buildAndExpand(soptMapId)
			.toUri();
		return ResponseEntity.created(location).body(CreateSoptMapResponse.from(soptMapId));
	}

	@Override
	@PutMapping("/{soptMapId}")
	public ResponseEntity<CreateSoptMapResponse> updateSoptMap(Principal principal,
		@PathVariable Long soptMapId,
		@RequestBody SoptMapUpdateRequest request) {
		soptMapRequestValidator.validate(request);

		Long updatedId = soptMapService.updateSoptMap(
			UserUtil.getUserId(principal),
			soptMapId,
			request.toDto());
		return ResponseEntity.ok(CreateSoptMapResponse.from(updatedId));
	}

	@Override
	@GetMapping("/search/subway")
	public ResponseEntity<SearchSubwayStationResponse> findSubwayStations(Principal principal,
		@RequestParam(required = false) String keyword) {
		return ResponseEntity.ok(SearchSubwayStationResponse.from(soptMapService.findSubwayStations(keyword)));
	}

	@Override
	@GetMapping
	public ResponseEntity<Page<SoptMapListResponseDto>> getSoptMapList(
		Principal principal,
		@RequestParam(required = false) MapTag category,
		@RequestParam(defaultValue = "LATEST") SortType sortType,
		@PageableDefault(size = 10, sort = "createdTimestamp", direction = Sort.Direction.DESC) Pageable pageable) {
		Integer userId = UserUtil.getUserId(principal);
		Page<SoptMapListResponseDto> result = soptMapService.getSoptMapList(
			userId,
			category,
			sortType,
			pageable);
		return ResponseEntity.ok(result);
	}
}
