package org.sopt.makers.crew.main.admin.v2.service.dto;

import java.util.Map;

import org.sopt.makers.crew.main.entity.property.Property;

public record PropertyDto(String key, Map<String, Object> properties) {
	public static PropertyDto from(Property property) {
		return new PropertyDto(property.getKey(), property.getProperties());
	}
}
