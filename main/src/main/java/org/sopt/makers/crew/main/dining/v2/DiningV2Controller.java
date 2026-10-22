package org.sopt.makers.crew.main.dining.v2;

import java.security.Principal;

import org.sopt.makers.crew.main.dining.v2.dto.request.DiningV2CreateAndUpdateDiningBodyDto;
import org.sopt.makers.crew.main.dining.v2.dto.request.DiningV2CreateAndUpdateRoundBodyDto;
import org.sopt.makers.crew.main.dining.v2.dto.response.DiningV2CreateDiningResponseDto;
import org.sopt.makers.crew.main.dining.v2.dto.response.DiningV2CreateRoundResponseDto;
import org.sopt.makers.crew.main.dining.v2.service.DiningV2Service;
import org.sopt.makers.crew.main.global.util.UserUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/dining/v2")
@RequiredArgsConstructor
public class DiningV2Controller implements DiningV2Api {

	private final DiningV2Service diningV2Service;

	@Override
	@PostMapping
	public ResponseEntity<DiningV2CreateDiningResponseDto> createDining(
		@Valid @RequestBody DiningV2CreateAndUpdateDiningBodyDto requestBody,
		Principal principal
	) {
		Integer userId = UserUtil.getUserId(principal);
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(diningV2Service.createDining(requestBody, userId));
	}

	@Override
	@PutMapping("/{meetingId}")
	public ResponseEntity<Void> updateDining(
		@PathVariable Integer meetingId,
		@Valid @RequestBody DiningV2CreateAndUpdateDiningBodyDto requestBody,
		Principal principal
	) {
		Integer userId = UserUtil.getUserId(principal);
		diningV2Service.updateDining(meetingId, requestBody, userId);
		return ResponseEntity.ok().build();
	}

	@Override
	@DeleteMapping("/{meetingId}")
	public ResponseEntity<Void> deleteDining(
		@PathVariable Integer meetingId,
		Principal principal
	) {
		Integer userId = UserUtil.getUserId(principal);
		diningV2Service.deleteDining(meetingId, userId);
		return ResponseEntity.noContent().build();
	}

	@Override
	@PostMapping("/{meetingId}/end")
	public ResponseEntity<Void> endDining(
		@PathVariable Integer meetingId,
		Principal principal
	) {
		Integer userId = UserUtil.getUserId(principal);
		diningV2Service.endDining(meetingId, userId);
		return ResponseEntity.ok().build();
	}

	@Override
	@PostMapping("/{meetingId}/rounds")
	public ResponseEntity<DiningV2CreateRoundResponseDto> createRound(
		@PathVariable Integer meetingId,
		@Valid @RequestBody DiningV2CreateAndUpdateRoundBodyDto requestBody,
		Principal principal
	) {
		Integer userId = UserUtil.getUserId(principal);
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(diningV2Service.createRound(meetingId, requestBody, userId));
	}

	@Override
	@PutMapping("/{meetingId}/rounds/{roundId}")
	public ResponseEntity<Void> updateRound(
		@PathVariable Integer meetingId,
		@PathVariable Integer roundId,
		@Valid @RequestBody DiningV2CreateAndUpdateRoundBodyDto requestBody,
		Principal principal
	) {
		Integer userId = UserUtil.getUserId(principal);
		diningV2Service.updateRound(meetingId, roundId, requestBody, userId);
		return ResponseEntity.ok().build();
	}

	@Override
	@DeleteMapping("/{meetingId}/rounds/{roundId}")
	public ResponseEntity<Void> deleteRound(
		@PathVariable Integer meetingId,
		@PathVariable Integer roundId,
		Principal principal
	) {
		Integer userId = UserUtil.getUserId(principal);
		diningV2Service.deleteRound(meetingId, roundId, userId);
		return ResponseEntity.noContent().build();
	}

	@Override
	@PostMapping("/{meetingId}/rounds/{roundId}/close")
	public ResponseEntity<Void> closeRound(
		@PathVariable Integer meetingId,
		@PathVariable Integer roundId,
		Principal principal
	) {
		Integer userId = UserUtil.getUserId(principal);
		diningV2Service.closeRound(meetingId, roundId, userId);
		return ResponseEntity.ok().build();
	}
}
