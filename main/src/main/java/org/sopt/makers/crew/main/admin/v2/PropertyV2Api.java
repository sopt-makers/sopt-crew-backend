package org.sopt.makers.crew.main.admin.v2;

import org.sopt.makers.crew.main.admin.v2.dto.HomePropertyResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "프로퍼티 조회")
public interface PropertyV2Api {

	@Operation(summary = "프로퍼티/홈 컨텐츠 조회")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "성공"),
		@ApiResponse(responseCode = "401", description = "유효하지 않는 토큰입니다.", content = @Content)
	})
	ResponseEntity<HomePropertyResponse> getHomeProperty();

	@Operation(summary = "키 값을 통한 단 건 조회(키 값이 없다면 전체 조회를 진행합니다.)")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "성공"),
		@ApiResponse(responseCode = "401", description = "유효하지 않는 토큰입니다.", content = @Content)
	})
	ResponseEntity<?> getProperty(@RequestParam(required = false) String key);

}
