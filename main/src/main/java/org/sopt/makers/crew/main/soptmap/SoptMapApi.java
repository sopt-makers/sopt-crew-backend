package org.sopt.makers.crew.main.soptmap;

import java.security.Principal;

import org.sopt.makers.crew.main.soptmap.dto.request.SoptMapRequest.CreateSoptMapRequest;
import org.sopt.makers.crew.main.soptmap.dto.request.SoptMapRequest.SoptMapUpdateRequest;
import org.sopt.makers.crew.main.soptmap.dto.response.SoptMapResponse.CreateSoptMapResponse;
import org.sopt.makers.crew.main.soptmap.dto.response.SoptMapResponse.SearchSubwayStationResponse;
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

	@Operation(summary = "솝맵 수정 api")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "성공"),
		@ApiResponse(responseCode = "403", description = "권한 없음"),
		@ApiResponse(responseCode = "404", description = "솝맵을 찾을 수 없음")
	})
	ResponseEntity<CreateSoptMapResponse> updateSoptMap(Principal principal, Long soptMapId,
		SoptMapUpdateRequest request);

	@Operation(summary = "지하철역 검색 api")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "성공"),
		@ApiResponse(responseCode = "403", description = "권한 없음"),
		@ApiResponse(responseCode = "404", description = "솝맵을 찾을 수 없음")
	})
	ResponseEntity<SearchSubwayStationResponse> findSubwayStations(Principal principal, String keyword);

}
