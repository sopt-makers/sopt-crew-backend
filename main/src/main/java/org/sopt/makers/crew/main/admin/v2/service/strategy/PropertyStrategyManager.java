package org.sopt.makers.crew.main.admin.v2.service.strategy;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.util.List;

import org.sopt.makers.crew.main.global.exception.NotFoundException;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PropertyStrategyManager {

	private final List<PropertyResponseStrategy<?>> strategies;

	@SuppressWarnings("unchecked")
	public <T> T createResponse(String key) {
		PropertyResponseStrategy<?> strategy = strategies.stream()
			.filter(s -> s.supports(key))
			.findFirst()
			.orElseThrow(() -> new NotFoundException(NOT_FOUND_PROPERTY_STRATEGY.getErrorCode()));

		return (T)strategy.createResponse(key);
	}
}
