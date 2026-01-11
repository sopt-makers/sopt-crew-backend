package org.sopt.makers.crew.main.soptmap;

import java.security.Principal;
import java.util.List;

import org.sopt.makers.crew.main.entity.soptmap.MapTag;
import org.sopt.makers.crew.main.soptmap.dto.SortType;
import org.sopt.makers.crew.main.soptmap.dto.request.SoptMapRequest.CheckEventWinningRequest;
import org.sopt.makers.crew.main.soptmap.dto.request.SoptMapRequest.CreateSoptMapRequest;
import org.sopt.makers.crew.main.soptmap.dto.request.SoptMapRequest.SoptMapUpdateRequest;
import org.sopt.makers.crew.main.soptmap.dto.response.SoptMapGetAllDto;
import org.sopt.makers.crew.main.soptmap.dto.response.SoptMapResponse.CreateSoptMapResponse;
import org.sopt.makers.crew.main.soptmap.dto.response.SoptMapResponse.SearchSubwayStationResponse;
import org.sopt.makers.crew.main.soptmap.dto.response.SoptMapResponse.SoptMapEventResponse;
import org.sopt.makers.crew.main.soptmap.dto.response.SoptMapResponse.ToggleSoptMapResponse;
import org.springframework.http.ResponseEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "솝맵")
public interface SoptMapApi {

	@Operation(summary = "솝맵 등록 api")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "성공")
	})
	ResponseEntity<CreateSoptMapResponse> createSoptMap(Principal principal,
		@Valid CreateSoptMapRequest request);

	@Operation(summary = "솝맵 수정 api")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "성공"),
		@ApiResponse(responseCode = "403", description = "권한 없음"),
		@ApiResponse(responseCode = "404", description = "솝맵을 찾을 수 없음")
	})
	ResponseEntity<CreateSoptMapResponse> updateSoptMap(Principal principal, Long soptMapId,
		@Valid SoptMapUpdateRequest request);

	@Operation(summary = "솝맵 삭제 api")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "성공"),
		@ApiResponse(responseCode = "403", description = "권한 없음"),
		@ApiResponse(responseCode = "404", description = "솝맵을 찾을 수 없음")
	})
	ResponseEntity<Void> deleteSoptMap(Principal principal, Long soptMapId);

	@Operation(summary = "지하철역 검색 api")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "성공"),
		@ApiResponse(responseCode = "403", description = "권한 없음"),
		@ApiResponse(responseCode = "404", description = "솝맵을 찾을 수 없음")
	})
	ResponseEntity<SearchSubwayStationResponse> findSubwayStations(Principal principal, String keyword);

	@Operation(summary = "솝맵 목록 조회/검색/필터링 api")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "성공")
	})
	ResponseEntity<SoptMapGetAllDto> getSoptMapList(
		@Parameter(hidden = true) Principal principal,
		@Parameter(description = "필터링할 카테고리 (null: 전체)", example = "FOOD, CAFE") List<MapTag> category,
		@Parameter(description = "정렬 타입", example = "LATEST") SortType sortType,
		@Parameter(description = "지하철역 검색어 (유사도 기반)", example = "강남") String stationKeyword,
		@Parameter(description = "페이지 번호 (1부터 시작)", example = "1") Integer page,
		@Parameter(description = "가져올 데이터 개수", example = "10") Integer take);

	@Operation(summary = "솝맵 추천하기 api")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "성공")
	})
	ResponseEntity<ToggleSoptMapResponse> recommendSoptMap(Principal principal,
		Long soptMapId);

	@Operation(summary = "이벤트 당첨 여부 확인")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "성공")
	})
	ResponseEntity<SoptMapEventResponse> eventSoptMap(CheckEventWinningRequest request);

}
