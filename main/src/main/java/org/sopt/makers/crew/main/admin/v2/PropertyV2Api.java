package org.sopt.makers.crew.main.admin.v2;

import java.util.List;

import org.sopt.makers.crew.main.admin.v2.dto.PropertyResponse;
import org.springframework.http.ResponseEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "프로퍼티 조회")
public interface PropertyV2Api {
	@Operation(summary = "프로퍼티/조회")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "성공"),
		@ApiResponse(responseCode = "401", description = "유효하지 않는 토큰입니다.", content = @Content)
	})
	ResponseEntity<List<PropertyResponse>> allProperties();
}
