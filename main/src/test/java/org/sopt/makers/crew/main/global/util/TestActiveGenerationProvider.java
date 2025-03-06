package org.sopt.makers.crew.main.global.util;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class TestActiveGenerationProvider implements ActiveGenerationProvider {
	private static final Integer FIX_ACTIVE_GENERATION = 35;

	@Override
	public Integer getActiveGeneration() {
		return FIX_ACTIVE_GENERATION;
	}
}
