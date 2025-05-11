package org.sopt.makers.crew.main.admin.v2;

import java.util.List;

import org.sopt.makers.crew.main.admin.v2.dto.HomePropertyResponse;
import org.sopt.makers.crew.main.admin.v2.dto.PropertyResponse;
import org.sopt.makers.crew.main.admin.v2.service.AdminService;
import org.sopt.makers.crew.main.admin.v2.service.JsonPrettierService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/property/v2")
@RequiredArgsConstructor
public class PropertyV2Controller implements PropertyV2Api {

	private final AdminService adminService;
	private final JsonPrettierService jsonPrettierService;

	@GetMapping("/all")
	public ResponseEntity<List<PropertyResponse>> allProperties() {
		return ResponseEntity.ok(adminService.findAllProperties().stream()
			.map(PropertyResponse::from)
			.toList());
	}

	@GetMapping("/home")
	public ResponseEntity<HomePropertyResponse> getHomeProperty() {
		return ResponseEntity.ok(
			HomePropertyResponse.from(
				jsonPrettierService.prettierHomeContent(adminService.findHomeProperties().propertyVos())));
	}

}
