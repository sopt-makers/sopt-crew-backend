package org.sopt.makers.crew.main.advertisement;

import java.security.Principal;

import org.sopt.makers.crew.main.advertisement.dto.AdvertisementGetResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "광고")
public interface AdvertisementApi {
	@Operation(summary = "광고 조회")
	@ResponseStatus(HttpStatus.OK)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "성공"),
	})
	ResponseEntity<AdvertisementGetResponseDto> getAdvertisement(Principal principal);

}
