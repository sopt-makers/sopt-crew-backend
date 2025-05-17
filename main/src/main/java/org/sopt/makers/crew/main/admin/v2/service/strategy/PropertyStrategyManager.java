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

	/**
	 * @return T 에 올 수 있는 값은 현재
	 * PropertyValueResponse :  key를 이용한 단 건 조회
	 * List<PropertyValueResponse> : 모든 프로퍼티 조회
	 */
	@SuppressWarnings("unchecked")
	public <T> T createResponse(String key) {
		PropertyResponseStrategy<?> strategy = strategies.stream()
			.filter(s -> s.supports(key))
			.findFirst()
			.orElseThrow(() -> new NotFoundException(NOT_FOUND_PROPERTY_STRATEGY.getErrorCode()));

		return (T)strategy.createResponse(key);
	}
}
