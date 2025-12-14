package org.sopt.makers.crew.main.soptmap;

import static org.sopt.makers.crew.main.soptmap.dto.request.SoptMapRequest.CreateSoptMapRequest;
import static org.sopt.makers.crew.main.soptmap.dto.request.SoptMapRequest.SoptMapUpdateRequest;

import java.net.URI;
import java.security.Principal;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import org.sopt.makers.crew.main.global.util.UserUtil;
import org.sopt.makers.crew.main.soptmap.dto.response.SoptMapResponse.CreateSoptMapResponse;
import org.sopt.makers.crew.main.soptmap.service.SoptMapService;
import org.sopt.makers.crew.main.soptmap.service.SoptMapRequestValidator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
			request.toDto()
		);
		return ResponseEntity.ok(CreateSoptMapResponse.from(updatedId));
	}
}
