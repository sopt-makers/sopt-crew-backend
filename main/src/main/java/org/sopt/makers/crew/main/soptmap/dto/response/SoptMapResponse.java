package org.sopt.makers.crew.main.soptmap.dto.response;

import java.util.List;

import org.sopt.makers.crew.main.soptmap.dto.ToggleSoptMapRecommendDto;
import org.sopt.makers.crew.main.soptmap.service.dto.SubwayStationDto;

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

	@Getter
	@RequiredArgsConstructor
	public static class SearchSubwayStationResponse {
		private final List<SubwayStationDto> stations;

		public static SearchSubwayStationResponse from(List<SubwayStationDto> subwayStationDtos) {
			return new SearchSubwayStationResponse(subwayStationDtos);
		}
	}

	@Getter
	@RequiredArgsConstructor
	public static class ToggleSoptMapResponse {
		private final Long soptMapId;
		private final Boolean toggleStatus;

		public static ToggleSoptMapResponse from(ToggleSoptMapRecommendDto toggleSoptMapRecommendDto) {
			return new ToggleSoptMapResponse(toggleSoptMapRecommendDto.soptMapId(),
				toggleSoptMapRecommendDto.isRecommended());
		}
	}
}
