package org.sopt.makers.crew.main.dining.v2;

import java.security.Principal;

import org.sopt.makers.crew.main.dining.v2.dto.request.DiningV2CreateAndUpdateDiningBodyDto;
import org.sopt.makers.crew.main.dining.v2.dto.request.DiningV2CreateAndUpdateRoundBodyDto;
import org.sopt.makers.crew.main.dining.v2.dto.response.DiningV2CreateDiningResponseDto;
import org.sopt.makers.crew.main.dining.v2.dto.response.DiningV2CreateRoundResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "회식")
public interface DiningV2Api {

	@Operation(summary = "회식 등록", description = "회식과 1차 회차를 생성하고, 개설자를 1차 참석자로 등록합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "meetingId: 10"),
		@ApiResponse(responseCode = "400", description = "VALIDATION_EXCEPTION, 지난 날짜로 회식을 등록할 수 없습니다., "
			+ "네이버지도 또는 카카오맵 링크만 등록할 수 있습니다.", content = @Content),
	})
	ResponseEntity<DiningV2CreateDiningResponseDto> createDining(
		@Valid @RequestBody DiningV2CreateAndUpdateDiningBodyDto requestBody,
		Principal principal);

	@Operation(summary = "회식 수정", description = "회식 정보와 1차 회차 정보를 수정합니다. 개설자만 가능합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "회식 수정 성공"),
		@ApiResponse(responseCode = "400", description = "VALIDATION_EXCEPTION, 종료된 회식입니다.", content = @Content),
		@ApiResponse(responseCode = "403", description = "권한이 없습니다.", content = @Content),
		@ApiResponse(responseCode = "404", description = "존재하지 않는 회식입니다.", content = @Content),
	})
	ResponseEntity<Void> updateDining(
		@PathVariable Integer meetingId,
		@Valid @RequestBody DiningV2CreateAndUpdateDiningBodyDto requestBody,
		Principal principal);

	@Operation(summary = "회식 삭제", description = "회식과 모든 회차, 참석 기록을 삭제합니다. 마감된 회차가 있거나 종료된 회식은 삭제할 수 없습니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "회식 삭제 성공"),
		@ApiResponse(responseCode = "400", description = "종료된 회식입니다., 마감된 회차가 있는 회식은 삭제할 수 없습니다.",
			content = @Content),
		@ApiResponse(responseCode = "403", description = "권한이 없습니다.", content = @Content),
		@ApiResponse(responseCode = "404", description = "존재하지 않는 회식입니다.", content = @Content),
	})
	ResponseEntity<Void> deleteDining(
		@PathVariable Integer meetingId,
		Principal principal);

	@Operation(summary = "회식 종료", description = "회식을 종료하고 마감하지 않은 회차를 함께 마감합니다. 종료 후에는 회차 추가와 회식·회차 수정이 불가합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "회식 종료 성공"),
		@ApiResponse(responseCode = "400", description = "종료된 회식입니다.", content = @Content),
		@ApiResponse(responseCode = "403", description = "권한이 없습니다.", content = @Content),
		@ApiResponse(responseCode = "404", description = "존재하지 않는 회식입니다.", content = @Content),
	})
	ResponseEntity<Void> endDining(
		@PathVariable Integer meetingId,
		Principal principal);

	@Operation(summary = "회차 추가", description = "새 회차를 추가하고 개설자를 참석자로 등록합니다. 이전 회차 참석자에게 알림을 보냅니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "roundId: 2"),
		@ApiResponse(responseCode = "400", description = "VALIDATION_EXCEPTION, 종료된 회식입니다.", content = @Content),
		@ApiResponse(responseCode = "403", description = "권한이 없습니다.", content = @Content),
		@ApiResponse(responseCode = "404", description = "존재하지 않는 회식입니다.", content = @Content),
	})
	ResponseEntity<DiningV2CreateRoundResponseDto> createRound(
		@PathVariable Integer meetingId,
		@Valid @RequestBody DiningV2CreateAndUpdateRoundBodyDto requestBody,
		Principal principal);

	@Operation(summary = "회차 수정", description = "회차의 장소, 링크, 시작 시간을 수정합니다. 마감된 회차도 수정할 수 있습니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "회차 수정 성공"),
		@ApiResponse(responseCode = "400", description = "VALIDATION_EXCEPTION, 종료된 회식입니다.", content = @Content),
		@ApiResponse(responseCode = "403", description = "권한이 없습니다.", content = @Content),
		@ApiResponse(responseCode = "404", description = "존재하지 않는 회식입니다., 존재하지 않는 회차입니다.", content = @Content),
	})
	ResponseEntity<Void> updateRound(
		@PathVariable Integer meetingId,
		@PathVariable Integer roundId,
		@Valid @RequestBody DiningV2CreateAndUpdateRoundBodyDto requestBody,
		Principal principal);

	@Operation(summary = "회차 삭제", description = "2차 이상 회차와 해당 회차의 참석 기록을 삭제합니다. 1차와 마감된 회차는 삭제할 수 없습니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "회차 삭제 성공"),
		@ApiResponse(responseCode = "400", description = "종료된 회식입니다., 1차는 회식 삭제로만 삭제할 수 있습니다., "
			+ "마감된 회차는 삭제할 수 없습니다.", content = @Content),
		@ApiResponse(responseCode = "403", description = "권한이 없습니다.", content = @Content),
		@ApiResponse(responseCode = "404", description = "존재하지 않는 회식입니다., 존재하지 않는 회차입니다.", content = @Content),
	})
	ResponseEntity<Void> deleteRound(
		@PathVariable Integer meetingId,
		@PathVariable Integer roundId,
		Principal principal);

	@Operation(summary = "회차 마감", description = "회차를 마감(확정)합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "회차 마감 성공"),
		@ApiResponse(responseCode = "400", description = "이미 마감된 회차입니다.", content = @Content),
		@ApiResponse(responseCode = "403", description = "권한이 없습니다.", content = @Content),
		@ApiResponse(responseCode = "404", description = "존재하지 않는 회식입니다., 존재하지 않는 회차입니다.", content = @Content),
	})
	ResponseEntity<Void> closeRound(
		@PathVariable Integer meetingId,
		@PathVariable Integer roundId,
		Principal principal);
}
