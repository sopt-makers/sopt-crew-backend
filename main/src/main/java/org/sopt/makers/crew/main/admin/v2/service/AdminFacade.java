package org.sopt.makers.crew.main.admin.v2.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.sopt.makers.crew.main.admin.v2.service.dto.AdminPagePresenter;
import org.sopt.makers.crew.main.admin.v2.service.dto.PropertyDto;
import org.sopt.makers.crew.main.admin.v2.service.vo.PropertyVo;
import org.sopt.makers.crew.main.entity.property.Property;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminFacade {

	private final AdminService adminService;
	private final JsonPrettierService jsonPrettierService;

	public AdminPagePresenter madeViewData() {

		List<PropertyVo> allProperties = adminService.findAllProperties().stream()
			.sorted(Comparator.comparing(Property::getKey))
			.map(PropertyVo::newInstance)
			.toList();

		Map<String, String> formattedJsonMap = jsonPrettierService.prettierJson(allProperties);

		return AdminPagePresenter.create(allProperties, formattedJsonMap);
	}

	public PropertyDto findPropertyByKey(String key) {
		return PropertyDto.from(adminService.findPropertyByKey(key));
	}

}
