package org.sopt.makers.crew.main.admin.v2.dto;

import java.util.Map;

import org.sopt.makers.crew.main.entity.property.Property;

public record PropertyResponse(String key, Map<String, Object> value) {

	public static PropertyResponse from(Property property) {
		return new PropertyResponse(property.getKey(), property.getProperties());
	}

	public static PropertyResponse from(PropertyDto dto) {
		return new PropertyResponse(dto.key(), dto.properties());
	}

}
