package org.sopt.makers.crew.main.global.util;

import org.sopt.makers.crew.main.global.constant.CrewConst;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class DefaultActiveGenerationProvider implements ActiveGenerationProvider {
	@Override
	public Integer getActiveGeneration() {
		return CrewConst.ACTIVE_GENERATION;
	}
}
