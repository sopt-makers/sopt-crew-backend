package org.sopt.makers.crew.main.admin.v2.service.strategy;

import java.util.List;
import java.util.Objects;

import org.sopt.makers.crew.main.admin.v2.dto.PropertyValueResponse;
import org.sopt.makers.crew.main.admin.v2.service.AdminService;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AllPropertiesStrategy implements PropertyResponseStrategy<List<PropertyValueResponse>> {

	private final AdminService adminService;

	@Override
	public List<PropertyValueResponse> createResponse(String key) {
		return adminService.findAllProperties().stream()
			.map(PropertyValueResponse::from)
			.toList();
	}

	@Override
	public boolean supports(String key) {
		return Objects.isNull(key);
	}
}
