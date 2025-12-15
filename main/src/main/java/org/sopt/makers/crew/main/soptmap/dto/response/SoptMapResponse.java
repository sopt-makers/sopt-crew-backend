package org.sopt.makers.crew.main.soptmap.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SoptMapResponse {

	@Getter
	@RequiredArgsConstructor
	public static class CreateSoptMapResponse {
		private final Long id;

		public static CreateSoptMapResponse from(Long id) {
			return new CreateSoptMapResponse(id);
		}
	}
}
