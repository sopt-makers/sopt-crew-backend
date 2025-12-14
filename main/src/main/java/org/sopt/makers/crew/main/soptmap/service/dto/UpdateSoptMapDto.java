package org.sopt.makers.crew.main.soptmap.service.dto;

import java.util.List;

import org.sopt.makers.crew.main.entity.soptmap.MapTag;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Builder
public class UpdateSoptMapDto {

	private final String placeName;
	private final List<String> stationNames;
	private final String description;
	private final List<MapTag> tags;
	private final String naverLink;
	private final String kakaoLink;

}
