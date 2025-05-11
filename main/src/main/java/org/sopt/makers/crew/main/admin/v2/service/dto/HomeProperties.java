package org.sopt.makers.crew.main.admin.v2.service.dto;

import java.util.List;

import org.sopt.makers.crew.main.admin.v2.service.vo.PropertyVo;

public record HomeProperties(List<PropertyVo> propertyVos) {
	public static HomeProperties from(List<PropertyVo> propertyVos) {
		return new HomeProperties(propertyVos);
	}
}
