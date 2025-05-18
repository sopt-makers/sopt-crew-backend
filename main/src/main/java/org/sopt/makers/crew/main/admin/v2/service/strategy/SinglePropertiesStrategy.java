package org.sopt.makers.crew.main.admin.v2.service.strategy;

import java.util.Objects;

import org.sopt.makers.crew.main.admin.v2.dto.PropertyValueResponse;
import org.sopt.makers.crew.main.admin.v2.service.AdminService;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SinglePropertiesStrategy implements PropertyResponseStrategy<PropertyValueResponse> {

	private final AdminService adminService;

	@Override
	public PropertyValueResponse createResponse(String key) {
		return PropertyValueResponse.from(
			adminService.findPropertyByKey(key)
		);
	}

	@Override
	public boolean supports(String key) {
		return !Objects.isNull(key);
	}
}
