package org.sopt.makers.crew.main.soptmap;

import static org.sopt.makers.crew.main.soptmap.request.SoptMapRequest.*;

import java.security.Principal;

import org.sopt.makers.crew.main.global.util.UserUtil;
import org.sopt.makers.crew.main.soptmap.response.SoptMapResponse.CreateSoptMapResponse;
import org.sopt.makers.crew.main.soptmap.service.CreateSoptMapService;
import org.sopt.makers.crew.main.soptmap.service.SoptMapRequestValidator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/map")
@RequiredArgsConstructor
public class SoptMapController implements SoptMapApi {

	private final SoptMapRequestValidator soptMapRequestValidator;
	private final CreateSoptMapService createSoptMapService;

	@Override
	@PostMapping
	public ResponseEntity<CreateSoptMapResponse> createSoptMap(Principal principal,
		@RequestBody CreateSoptMapRequest request) {
		soptMapRequestValidator.validate(request);
		return ResponseEntity.ok(
			CreateSoptMapResponse.createSoptMapResponse(
				createSoptMapService.createSoptMap(UserUtil.getUserId(principal), request.toDto()))
		);
	}
}
