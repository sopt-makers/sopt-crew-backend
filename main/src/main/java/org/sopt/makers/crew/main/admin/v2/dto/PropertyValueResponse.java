package org.sopt.makers.crew.main.admin.v2.dto;

import java.util.Map;

import org.sopt.makers.crew.main.entity.property.Property;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.annotation.JsonValue;

public record PropertyValueResponse(@JsonUnwrapped Map<String, Object> value) {
	public static PropertyValueResponse from(Property property) {
		return new PropertyValueResponse(property.getProperties());
	}

	@JsonValue
	public Map<String, Object> getValue() {
		return value;
	}
}

