package org.sopt.makers.crew.main.soptmap;

import java.security.Principal;

import org.sopt.makers.crew.main.soptmap.dto.request.SoptMapRequest.CreateSoptMapRequest;
import org.sopt.makers.crew.main.soptmap.dto.response.SoptMapResponse.CreateSoptMapResponse;
import org.springframework.http.ResponseEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "솝맵")
public interface SoptMapApi {

	@Operation(summary = "솝맵 등록 api")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "성공")
	})
	ResponseEntity<CreateSoptMapResponse> createSoptMap(Principal principal,
		CreateSoptMapRequest request);

}
