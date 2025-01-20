package org.sopt.makers.crew.main.lightning.v2;

import java.security.Principal;

import org.sopt.makers.crew.main.global.util.UserUtil;
import org.sopt.makers.crew.main.lightning.v2.dto.request.LightningV2CreateLightningBodyDto;
import org.sopt.makers.crew.main.lightning.v2.dto.response.LightningV2CreateLightningResponseDto;
import org.sopt.makers.crew.main.lightning.v2.service.LightningV2Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/lightning/v2")
@RequiredArgsConstructor
public class LightningV2Controller implements LightningV2Api {
	private final LightningV2Service lightningV2Service;

	@Override
	@PostMapping
	public ResponseEntity<LightningV2CreateLightningResponseDto> createLightning(
		@Valid @RequestBody LightningV2CreateLightningBodyDto requestBody,
		Principal principal
	) {
		Integer userId = UserUtil.getUserId(principal);
		return ResponseEntity.status(HttpStatus.CREATED).body(lightningV2Service.createLightning(requestBody, userId));
	}

	// @Override
	// @GetMapping("{lightningId}")
	// public ResponseEntity<LightningV2GetLightningByIdResponseDto> getLightningById(
	// 	@PathVariable Integer lightningId,
	// 	Principal principal
	// ) {
	// 	Integer userId = UserUtil.getUserId(principal);
	//
	// 	return ResponseEntity.ok(lightningV2Service.getLightningById(lightningId, userId));
	// }
}
