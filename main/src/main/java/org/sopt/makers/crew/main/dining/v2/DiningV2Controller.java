package org.sopt.makers.crew.main.dining.v2;

import java.security.Principal;

import org.sopt.makers.crew.main.dining.v2.dto.query.DiningV2GetAttendeesQueryDto;
import org.sopt.makers.crew.main.dining.v2.dto.query.DiningV2GetDiningsQueryDto;
import org.sopt.makers.crew.main.dining.v2.dto.request.DiningV2AddAttendeeBodyDto;
import org.sopt.makers.crew.main.dining.v2.dto.request.DiningV2CreateAndUpdateDiningBodyDto;
import org.sopt.makers.crew.main.dining.v2.dto.request.DiningV2CreateAndUpdateRoundBodyDto;
import org.sopt.makers.crew.main.dining.v2.dto.response.DiningV2CreateDiningResponseDto;
import org.sopt.makers.crew.main.dining.v2.dto.response.DiningV2CreateRoundResponseDto;
import org.sopt.makers.crew.main.dining.v2.dto.response.DiningV2GetAttendanceResponseDto;
import org.sopt.makers.crew.main.dining.v2.dto.response.DiningV2GetAttendeeManagementResponseDto;
import org.sopt.makers.crew.main.dining.v2.dto.response.DiningV2GetAttendeesResponseDto;
import org.sopt.makers.crew.main.dining.v2.dto.response.DiningV2GetPayerRequestsResponseDto;
import org.sopt.makers.crew.main.dining.v2.dto.response.DiningV2GetDiningDetailResponseDto;
import org.sopt.makers.crew.main.dining.v2.dto.response.DiningV2GetDiningsResponseDto;
import org.sopt.makers.crew.main.dining.v2.service.DiningV2Service;
import org.sopt.makers.crew.main.global.util.UserUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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

	@Override
	@PostMapping("/{meetingId}/rounds/{roundId}/attendance")
	public ResponseEntity<Void> applyAttendance(
		@PathVariable Integer meetingId,
		@PathVariable Integer roundId,
		Principal principal) {
		diningV2Service.applyAttendance(meetingId, roundId, UserUtil.getUserId(principal));
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@Override
	@DeleteMapping("/{meetingId}/rounds/{roundId}/attendance")
	public ResponseEntity<Void> cancelAttendance(
		@PathVariable Integer meetingId,
		@PathVariable Integer roundId,
		Principal principal) {
		diningV2Service.cancelAttendance(meetingId, roundId, UserUtil.getUserId(principal));
		return ResponseEntity.noContent().build();
	}

	@Override
	@GetMapping("/{meetingId}/rounds/{roundId}/attendance")
	public ResponseEntity<DiningV2GetAttendanceResponseDto> getAttendance(
		@PathVariable Integer meetingId,
		@PathVariable Integer roundId,
		Principal principal) {
		return ResponseEntity.ok(diningV2Service.getAttendance(meetingId, roundId, UserUtil.getUserId(principal)));
	}

	@Override
	@GetMapping("/{meetingId}/rounds/{roundId}/attendees")
	public ResponseEntity<DiningV2GetAttendeeManagementResponseDto> getAttendees(
		@PathVariable Integer meetingId,
		@PathVariable Integer roundId,
		@Valid @ModelAttribute DiningV2GetAttendeesQueryDto queryCommand,
		Principal principal) {
		return ResponseEntity.ok(diningV2Service.getAttendees(meetingId, roundId, queryCommand, UserUtil.getUserId(principal)));
	}

	@Override
	@PostMapping("/{meetingId}/rounds/{roundId}/attendees")
	public ResponseEntity<Void> addAttendee(
		@PathVariable Integer meetingId,
		@PathVariable Integer roundId,
		@Valid @RequestBody DiningV2AddAttendeeBodyDto requestBody,
		Principal principal) {
		diningV2Service.addAttendee(meetingId, roundId, requestBody.userId(), UserUtil.getUserId(principal));
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@Override
	@DeleteMapping("/{meetingId}/rounds/{roundId}/attendees/{attendeeUserId}")
	public ResponseEntity<Void> deleteAttendee(
		@PathVariable Integer meetingId,
		@PathVariable Integer roundId,
		@PathVariable Integer attendeeUserId,
		Principal principal) {
		diningV2Service.deleteAttendee(meetingId, roundId, attendeeUserId, UserUtil.getUserId(principal));
		return ResponseEntity.noContent().build();
	}

	@Override
	@GetMapping("/{meetingId}/rounds/{roundId}/attendance/attendees")
	public ResponseEntity<DiningV2GetAttendeesResponseDto> getAttendanceAttendees(
		@PathVariable Integer meetingId, @PathVariable Integer roundId, Principal principal) {
		return ResponseEntity.ok(diningV2Service.getAttendanceAttendees(meetingId, roundId,
			UserUtil.getUserId(principal)));
	}

	@Override
	@PostMapping("/{meetingId}/rounds/{roundId}/payer-requests")
	public ResponseEntity<Void> requestPayer(@PathVariable Integer meetingId, @PathVariable Integer roundId,
		Principal principal) {
		diningV2Service.requestPayer(meetingId, roundId, UserUtil.getUserId(principal));
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@Override
	@GetMapping("/{meetingId}/payer-requests")
	public ResponseEntity<DiningV2GetPayerRequestsResponseDto> getPayerRequests(@PathVariable Integer meetingId,
		Principal principal) {
		return ResponseEntity.ok(diningV2Service.getPayerRequests(meetingId, UserUtil.getUserId(principal)));
	}

	@Override
	@PostMapping("/{meetingId}/rounds/{roundId}/payer-requests/{requesterUserId}/approve")
	public ResponseEntity<Void> approvePayer(@PathVariable Integer meetingId, @PathVariable Integer roundId,
		@PathVariable Integer requesterUserId, Principal principal) {
		diningV2Service.approvePayer(meetingId, roundId, requesterUserId, UserUtil.getUserId(principal));
		return ResponseEntity.ok().build();
	}

	@Override
	@PostMapping("/{meetingId}/rounds/{roundId}/payer-requests/{requesterUserId}/reject")
	public ResponseEntity<Void> rejectPayer(@PathVariable Integer meetingId, @PathVariable Integer roundId,
		@PathVariable Integer requesterUserId, Principal principal) {
		diningV2Service.rejectPayer(meetingId, roundId, requesterUserId, UserUtil.getUserId(principal));
		return ResponseEntity.ok().build();
	}

	@Override
	@PostMapping("/{meetingId}/rounds/{roundId}/payer-requests/{requesterUserId}/revoke")
	public ResponseEntity<Void> revokePayer(@PathVariable Integer meetingId, @PathVariable Integer roundId,
		@PathVariable Integer requesterUserId, Principal principal) {
		diningV2Service.revokePayer(meetingId, roundId, requesterUserId, UserUtil.getUserId(principal));
		return ResponseEntity.ok().build();

	@GetMapping
	public ResponseEntity<DiningV2GetDiningsResponseDto> getDinings(
		@Valid @ModelAttribute DiningV2GetDiningsQueryDto queryCommand,
		Principal principal
	) {
		Integer userId = UserUtil.getUserId(principal);
		return ResponseEntity.ok(diningV2Service.getDinings(queryCommand, userId));
	}

	@Override
	@GetMapping("/{meetingId}")
	public ResponseEntity<DiningV2GetDiningDetailResponseDto> getDiningDetail(
		@PathVariable Integer meetingId,
		Principal principal
	) {
		Integer userId = UserUtil.getUserId(principal);
		return ResponseEntity.ok(diningV2Service.getDiningDetail(meetingId, userId));
	}
}
