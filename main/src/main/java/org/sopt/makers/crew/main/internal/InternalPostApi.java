package org.sopt.makers.crew.main.internal;

import org.sopt.makers.crew.main.global.pagination.dto.PageOptionsDto;
import org.sopt.makers.crew.main.internal.dto.InternalPostGetAllResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.sopt.makers.crew.main.internal.dto.InternalPostCreateRequestDto;
import org.sopt.makers.crew.main.internal.dto.InternalPostCreateResponseDto;
import org.sopt.makers.crew.main.internal.dto.InternalPostGetAllResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "[Internal] 피드")
public interface InternalPostApi {

	@Operation(summary = "[Internal] 피드 전체 조회", description = "플그 모임 탭에서의 모임 피드를 보여주기 위한 조회 api")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "모임 목록 조회 성공")})
	@Parameters(value = {
		@Parameter(name = "page", description = "페이지", example = "1", required = true, schema = @Schema(type = "integer", format = "int32")),
		@Parameter(name = "take", description = "가져올 데이터 개수", example = "10", required = true, schema = @Schema(type = "integer", format = "int32")),
	})
	ResponseEntity<InternalPostGetAllResponseDto> getPosts(
		@Parameter(description = "플레이그라운드 유저 ID(orgId)", example = "1") Integer orgId,
		@ModelAttribute @Valid @Parameter(hidden = true) PageOptionsDto pageOptionsDto
	);


	@Operation(summary = "[Internal] 피드 생성", description = "플그 모임 탭에서 피드를 생성하기 위한 api")
	@ApiResponses(value = {@ApiResponse(responseCode = "201", description = "피드 생성 성공")})
	ResponseEntity<InternalPostCreateResponseDto> createPost(
		@PathVariable Integer orgId,
		@RequestBody @Valid InternalPostCreateRequestDto requestDto
	);
}
