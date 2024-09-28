package org.sopt.makers.crew.main.global.util;

import java.time.LocalDateTime;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class StubTime implements Time {
	@Override
	public LocalDateTime now() {
		return LocalDateTime.of(2024, 4, 24, 23, 59);
	}
}
