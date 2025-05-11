package org.sopt.makers.crew.main.admin.v2.service.vo;

import java.util.Map;

import org.sopt.makers.crew.main.entity.property.Property;

public record PropertyVo(String key, Map<String, Object> properties) {
	public static PropertyVo newInstance(Property property) {
		return new PropertyVo(property.getKey(), property.getProperties());
	}
}
