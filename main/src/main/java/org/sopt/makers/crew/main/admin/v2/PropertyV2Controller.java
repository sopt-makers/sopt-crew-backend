package org.sopt.makers.crew.main.admin.v2;

import org.sopt.makers.crew.main.admin.v2.dto.HomePropertyResponse;
import org.sopt.makers.crew.main.admin.v2.service.AdminService;
import org.sopt.makers.crew.main.admin.v2.service.JsonPrettierService;
import org.sopt.makers.crew.main.admin.v2.service.strategy.PropertyStrategyManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/property/v2")
@RequiredArgsConstructor
public class PropertyV2Controller implements PropertyV2Api {

	private final AdminService adminService;
	private final JsonPrettierService jsonPrettierService;
	private final PropertyStrategyManager strategyManager;

	@GetMapping("/home")
	public ResponseEntity<HomePropertyResponse> getHomeProperty() {
		return ResponseEntity.ok(
			HomePropertyResponse.from(
				jsonPrettierService.prettierHomeContent(adminService.findHomeProperties().propertyVos())));
	}

	@GetMapping
	public ResponseEntity<?> getProperty(@RequestParam(required = false) String key) {
		return ResponseEntity.ok(strategyManager.createResponse(key));
	}

}
