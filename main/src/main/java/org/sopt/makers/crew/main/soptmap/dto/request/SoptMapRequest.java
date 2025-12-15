package org.sopt.makers.crew.main.soptmap.dto.request;

import java.util.List;

import org.sopt.makers.crew.main.entity.soptmap.MapTag;
import org.sopt.makers.crew.main.soptmap.service.dto.CreateSoptMapDto;
import org.sopt.makers.crew.main.soptmap.service.dto.UpdateSoptMapDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SoptMapRequest {
	public interface Validatable {
		List<MapTag> getTags();

		List<String> getStationNames();
	}

	@Getter
	@RequiredArgsConstructor
	public static class CreateSoptMapRequest implements Validatable {

		@Schema(description = "장소 이름", example = "온더플랜")
		@NotNull
		private final String placeName;
		@Schema(description = "주변 역 이름들", example = "[\"강남역\", \"건대입구역\"]")
		@NotNull
		private final List<String> stationNames;
		@Schema(description = "한줄 소개", example = "장소 너무 좋아요")
		@NotNull
		private final String description;
		@Schema(description = "장소 태그", example = "[\"FOOD\", \"CAFE\"]")
		@NotNull
		private final List<MapTag> tags;
		@Schema(description = "네이버 지도 링크", example = "https://map~~~~")
		private final String naverLink;
		@Schema(description = "카카오맵 링크 태그", example = "https://map~~~~")
		private final String kakaoLink;

		public CreateSoptMapDto toDto() {
			return CreateSoptMapDto.builder()
				.placeName(placeName)
				.stationNames(stationNames)
				.description(description)
				.tags(tags)
				.naverLink(naverLink)
				.kakaoLink(kakaoLink)
				.build();
		}
	}

	@Getter
	@RequiredArgsConstructor
	public static class SoptMapUpdateRequest implements Validatable {

		@Schema(description = "장소 이름", example = "온더플랜")
		@NotNull
		private final String placeName;
		@Schema(description = "주변 역 이름들", example = "[\"강남역\", \"건대입구역\"]")
		@NotNull
		private final List<String> stationNames;
		@Schema(description = "한줄 소개", example = "장소 너무 좋아요")
		@NotNull
		private final String description;
		@Schema(description = "장소 태그", example = "[\"FOOD\", \"CAFE\"]")
		@NotNull
		private final List<MapTag> tags;
		@Schema(description = "네이버 지도 링크", example = "https://map~~~~")
		private final String naverLink;
		@Schema(description = "카카오맵 링크 태그", example = "https://map~~~~")
		private final String kakaoLink;

		public UpdateSoptMapDto toDto() {
			return UpdateSoptMapDto.builder()
				.placeName(placeName)
				.stationNames(stationNames)
				.description(description)
				.tags(tags)
				.naverLink(naverLink)
				.kakaoLink(kakaoLink)
				.build();
		}
	}
}
