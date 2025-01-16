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
		@ApiResponse(responseCode = "201", description = "성공"),
		@ApiResponse(responseCode = "400", description = "\"이미지 파일이 없습니다.\" or \"한 개 이상의 파트를 입력해주세요\" or \"프로필을 입력해주세요\"", content = @Content),
	})
	ResponseEntity<LightningV2CreateLightningResponseDto> createLightning(
		@Valid @RequestBody LightningV2CreateLightningBodyDto requestBody,
		Principal principal);
}
