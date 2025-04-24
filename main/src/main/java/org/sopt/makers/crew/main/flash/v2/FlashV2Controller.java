package org.sopt.makers.crew.main.flash.v2;

import java.security.Principal;

import org.sopt.makers.crew.main.flash.v2.dto.request.FlashV2CreateAndUpdateFlashBodyDto;
import org.sopt.makers.crew.main.flash.v2.dto.response.FlashV2CreateAndUpdateResponseDto;
import org.sopt.makers.crew.main.flash.v2.dto.response.FlashV2GetFlashByMeetingIdResponseDto;
import org.sopt.makers.crew.main.flash.v2.service.FlashV2Service;
import org.sopt.makers.crew.main.global.util.UserUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/flash/v2")
@RequiredArgsConstructor
public class FlashV2Controller implements FlashV2Api {
	private final FlashV2Service flashV2Service;

	@Override
	@PostMapping
	public ResponseEntity<FlashV2CreateAndUpdateResponseDto> createFlash(
		@Valid @RequestBody FlashV2CreateAndUpdateFlashBodyDto requestBody,
		Principal principal
	) {
		Integer userId = UserUtil.getUserId(principal);
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(flashV2Service.createFlash(requestBody, userId));
	}

	@Override
	@GetMapping("{meetingId}")
	public ResponseEntity<FlashV2GetFlashByMeetingIdResponseDto> getFlashByMeetingId(
		@PathVariable Integer meetingId,
		Principal principal
	) {
		Integer userId = UserUtil.getUserId(principal);

		return ResponseEntity.ok(flashV2Service.getFlashDetail(meetingId, userId));
	}

	@Override
	@PutMapping("{meetingId}")
	public ResponseEntity<Void> updateFlash(
		@PathVariable Integer meetingId,
		@Valid @RequestBody FlashV2CreateAndUpdateFlashBodyDto requestBody,
		Principal principal
	) {
		Integer userId = UserUtil.getUserId(principal);
		flashV2Service.updateFlash(meetingId, requestBody, userId);
		return ResponseEntity.ok().build();
	}
}
