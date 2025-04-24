package org.sopt.makers.crew.main.flash.v2;

import java.security.Principal;

import org.sopt.makers.crew.main.flash.v2.dto.request.FlashV2CreateAndUpdateFlashBodyDto;
import org.sopt.makers.crew.main.flash.v2.dto.response.FlashV2CreateAndUpdateResponseDto;
import org.sopt.makers.crew.main.flash.v2.dto.response.FlashV2GetFlashByMeetingIdResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "번쩍 모임")
public interface FlashV2Api {

	@Operation(summary = "번쩍 모임 생성")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "meetingId: 10, tagId: 4"),
		@ApiResponse(responseCode = "400", description = "VALIDATION_EXCEPTION", content = @Content),
	})
	ResponseEntity<FlashV2CreateAndUpdateResponseDto> createFlash(
		@Valid @RequestBody FlashV2CreateAndUpdateFlashBodyDto requestBody,
		Principal principal);

	@Operation(summary = "번쩍 모임 상세 조회")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "번쩍 모임 상세 조회 성공"),
		@ApiResponse(responseCode = "400", description = "번쩍 모임이 없습니다.", content = @Content),
	})
	ResponseEntity<FlashV2GetFlashByMeetingIdResponseDto> getFlashByMeetingId(
		@PathVariable Integer meetingId,
		Principal principal);

	@Operation(summary = "번쩍 모임 수정")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "meetingId: 10, tagId: 5"),
		@ApiResponse(responseCode = "400", description = "VALIDATION_EXCEPTION", content = @Content),
	})
	ResponseEntity<Void> updateFlash(
		@PathVariable Integer meetingId,
		@Valid @RequestBody FlashV2CreateAndUpdateFlashBodyDto requestBody,
		Principal principal);
}
