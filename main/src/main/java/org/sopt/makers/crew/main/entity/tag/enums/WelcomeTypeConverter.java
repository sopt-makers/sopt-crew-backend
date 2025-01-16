package org.sopt.makers.crew.main.entity.tag.enums;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class WelcomeTypeConverter implements AttributeConverter<List<WelcomeMessageType>, String> {
	private static final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public String convertToDatabaseColumn(List<WelcomeMessageType> attribute) {
		try {
			return objectMapper.writeValueAsString(attribute);
		} catch (Exception e) {
			throw new IllegalArgumentException("Error converting list to JSON: " + attribute, e);
		}
	}

	@Override
	public List<WelcomeMessageType> convertToEntityAttribute(String dbData) {
		try {
			return objectMapper.readValue(dbData, new TypeReference<>() {
			});
		} catch (Exception e) {
			throw new IllegalArgumentException("Error converting JSON to list: " + dbData, e);
		}
	}
}
