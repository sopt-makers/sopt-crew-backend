package org.sopt.makers.crew.main.lightning.v2;

import java.security.Principal;

import org.sopt.makers.crew.main.lightning.v2.dto.request.LightningV2CreateLightningBodyDto;
import org.sopt.makers.crew.main.lightning.v2.dto.response.LightningV2CreateLightningResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "번쩍 모임")
public interface LightningV2Api {

	@Operation(summary = "번쩍 모임 생성")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "lightningId: 10"),
		@ApiResponse(responseCode = "400", description = "VALIDATION_EXCEPTION", content = @Content),
	})
	ResponseEntity<LightningV2CreateLightningResponseDto> createLightning(
		@Valid @RequestBody LightningV2CreateLightningBodyDto requestBody,
		Principal principal);

	// @Operation(summary = "번쩍 모임 상세 조회")
	// @ApiResponses(value = {
	// 	@ApiResponse(responseCode = "200", description = "번쩍 모임 상세 조회 성공"),
	// 	@ApiResponse(responseCode = "400", description = "번쩍 모임이 없습니다.", content = @Content),
	// })
	// ResponseEntity<LightningV2GetLightningByIdResponseDto> getLightningById(
	// 	@PathVariable Integer lightningId,
	// 	Principal principal);
}
