package org.sopt.makers.crew.main.global.util;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataConverter {

	private final ObjectMapper objectMapper;

	public <T> T convertValue(Object value, Class<T> clazz) {
		return objectMapper.convertValue(value, clazz);
	}

	public <T> T convertValue(Object value, TypeReference<T> clazz) {
		return objectMapper.convertValue(value, clazz);
	}
}
