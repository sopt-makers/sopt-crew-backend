package org.sopt.makers.crew.main.global.util;

import org.sopt.makers.crew.main.global.constant.CrewConst;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Profile("!test")
@Slf4j
public class DefaultActiveGenerationProvider implements ActiveGenerationProvider {
	@Override
	public Integer getActiveGeneration() {
		log.info("now generation : {}", CrewConst.ACTIVE_GENERATION);
		return CrewConst.ACTIVE_GENERATION;
	}
}
