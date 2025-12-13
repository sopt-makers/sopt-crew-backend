package org.sopt.makers.crew.main.soptmap.dto;

import java.util.List;

import org.sopt.makers.crew.main.entity.soptmap.MapTag;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class CreateSoptMapDto {

	private final String placeName;
	private final List<String> stationNames;
	private final String description;
	private final List<MapTag> tags;
	private final String naverLink;
	private final String kakaoLink;

}
