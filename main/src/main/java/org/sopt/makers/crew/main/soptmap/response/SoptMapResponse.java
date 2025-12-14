package org.sopt.makers.crew.main.soptmap.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class SoptMapResponse {

	@Getter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class CreateSoptMapResponse {
		private Long id;

		public static CreateSoptMapResponse createSoptMapResponse(Long id) {
			return new CreateSoptMapResponse(id);
		}
	}
}
