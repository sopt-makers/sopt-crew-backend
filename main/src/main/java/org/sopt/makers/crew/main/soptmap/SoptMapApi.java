package org.sopt.makers.crew.main.soptmap;

import java.security.Principal;

import org.sopt.makers.crew.main.entity.soptmap.MapTag;
import org.sopt.makers.crew.main.soptmap.dto.SortType;
import org.sopt.makers.crew.main.soptmap.dto.request.SoptMapRequest.CreateSoptMapRequest;
import org.sopt.makers.crew.main.soptmap.dto.request.SoptMapRequest.SoptMapUpdateRequest;
import org.sopt.makers.crew.main.soptmap.dto.response.SoptMapListResponseDto;
import org.sopt.makers.crew.main.soptmap.dto.response.SoptMapResponse.CreateSoptMapResponse;
import org.sopt.makers.crew.main.soptmap.dto.response.SoptMapResponse.SearchSubwayStationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

	@Operation(summary = "솝맵 목록 조회 api (페이지네이션)")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "성공")
	})
	ResponseEntity<Page<SoptMapListResponseDto>> getSoptMapList(
		Principal principal,
		@Parameter(description = "필터링할 카테고리", example = "FOOD") MapTag category,
		@Parameter(description = "정렬 타입 (LATEST, POPULAR)", example = "LATEST") SortType sortType,
		Pageable pageable);
}
