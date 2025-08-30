package org.sopt.makers.crew.main.auth.v2;

import org.sopt.makers.crew.main.auth.v2.dto.response.AuthV2ResponseDto;
import org.sopt.makers.crew.main.global.annotation.AuthenticatedUserId;
import org.springframework.http.ResponseEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "인증")
public interface AuthV2Api {
	@Operation(summary = "로그인/회원가입")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "성공"),
		@ApiResponse(responseCode = "401", description = "유효하지 않는 토큰입니다.", content = @Content),
		@ApiResponse(responseCode = "500", description = "크루 서버 또는 플레이그라운드 서버 오류입니다.", content = @Content),
	})
	ResponseEntity<AuthV2ResponseDto> loginUser(
		@AuthenticatedUserId Integer userId);
}
