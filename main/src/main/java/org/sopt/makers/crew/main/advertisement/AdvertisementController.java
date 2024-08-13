package org.sopt.makers.crew.main.advertisement;

import java.security.Principal;

import org.sopt.makers.crew.main.advertisement.dto.AdvertisementGetResponseDto;
import org.sopt.makers.crew.main.advertisement.service.AdvertisementService;
import org.sopt.makers.crew.main.entity.advertisement.enums.AdvertisementCategory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/advertisement/v2")
@RequiredArgsConstructor
public class AdvertisementController implements AdvertisementApi {

	private final AdvertisementService advertisementService;

	@Override
	@GetMapping
	public ResponseEntity<AdvertisementGetResponseDto> getAdvertisement(
		@RequestParam(name = "category") AdvertisementCategory category,
		Principal principal) {

		AdvertisementGetResponseDto response = advertisementService.getAdvertisement(category);

		return ResponseEntity.ok().body(response);
	}
}
