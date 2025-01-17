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
			// Enum -> 한글 값 리스트 -> JSON
			List<String> values = attribute.stream()
				.map(WelcomeMessageType::getValue)
				.toList();
			return objectMapper.writeValueAsString(values);
		} catch (Exception e) {
			throw new IllegalArgumentException("Error converting list to JSON: " + attribute, e);
		}
	}

	@Override
	public List<WelcomeMessageType> convertToEntityAttribute(String dbData) {
		try {
			// JSON -> 한글 값 리스트 -> Enum
			List<String> values = objectMapper.readValue(dbData, new TypeReference<>() {
			});
			return values.stream()
				.map(WelcomeMessageType::ofValue)
				.toList();
		} catch (Exception e) {
			throw new IllegalArgumentException("Error converting JSON to list: " + dbData, e);
		}
	}
}
