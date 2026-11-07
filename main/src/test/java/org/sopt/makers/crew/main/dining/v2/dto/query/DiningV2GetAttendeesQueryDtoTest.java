package org.sopt.makers.crew.main.dining.v2.dto.query;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;

class DiningV2GetAttendeesQueryDtoTest {

	@Test
	void defaultsAndAllowedValues() {
		var defaults = new DiningV2GetAttendeesQueryDto(null, null, null);
		assertThat(defaults.getPage()).isEqualTo(1);
		assertThat(defaults.getTake()).isEqualTo(10);
		assertThat(defaults.getDate()).isEqualTo("desc");
		try (var factory = Validation.buildDefaultValidatorFactory()) {
			var validator = factory.getValidator();
			for (int take : new int[] {10, 30, 50}) {
				assertThat(validator.validate(new DiningV2GetAttendeesQueryDto(1, take, "asc"))).isEmpty();
			}
			for (int take : new int[] {0, 12, 20, 51}) {
				assertThat(validator.validate(new DiningV2GetAttendeesQueryDto(1, take, "desc"))).isNotEmpty();
			}
			assertThat(validator.validate(new DiningV2GetAttendeesQueryDto(0, 10, "desc"))).isNotEmpty();
			assertThat(validator.validate(new DiningV2GetAttendeesQueryDto(1, 10, "invalid"))).isNotEmpty();
		}
	}
}
