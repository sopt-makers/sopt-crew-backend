package org.sopt.makers.crew.main.admin.v2;

import java.util.List;

import org.sopt.makers.crew.main.admin.v2.dto.PropertyResponse;
import org.sopt.makers.crew.main.admin.v2.service.AdminService;
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

	@GetMapping("/all")
	public ResponseEntity<List<PropertyResponse>> allProperties() {
		return ResponseEntity.ok(adminService.findAllProperties().stream()
			.map(PropertyResponse::from)
			.toList());
	}

}
