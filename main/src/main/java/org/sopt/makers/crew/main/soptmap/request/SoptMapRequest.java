package org.sopt.makers.crew.main.soptmap.request;

import java.util.List;

import org.sopt.makers.crew.main.entity.soptmap.MapTag;
import org.sopt.makers.crew.main.soptmap.dto.CreateSoptMapDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class SoptMapRequest {

	@Getter
	@Setter
	@NoArgsConstructor
	public static class CreateSoptMapRequest {

		@Schema(description = "장소 이름", example = "온더플랜")
		@NotNull
		private String placeName;
		@Schema(description = "주변 역 이름들", example = "['강남역', '건대역']")
		@NotNull
		private List<String> stationNames;
		@Schema(description = "한줄 소개", example = "장소 너무 좋아요")
		@NotNull
		private String description;
		@Schema(description = "장소 태그", example = "['FOOD', 'CAFE', 'ETC']")
		@NotNull
		private List<MapTag> tags;
		@Schema(description = "네이버 지도 링크", example = "https://map~~~~")
		private String naverLink;
		@Schema(description = "카카오맵 링크 태그", example = "https://map~~~~")
		private String kakaoLink;

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
}
