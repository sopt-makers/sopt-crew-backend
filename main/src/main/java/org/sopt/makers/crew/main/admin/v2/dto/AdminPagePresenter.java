package org.sopt.makers.crew.main.admin.v2.dto;

import java.util.List;
import java.util.Map;

import org.sopt.makers.crew.main.admin.v2.service.vo.PropertyVo;

public record AdminPagePresenter(List<PropertyVo> properties, Map<String, String> formattedJson) {
	public static AdminPagePresenter create(List<PropertyVo> properties, Map<String, String> formattedJson) {
		return new AdminPagePresenter(properties, formattedJson);
	}
}
